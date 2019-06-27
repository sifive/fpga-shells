# fpga-shells

An FPGA shell is a Chisel module designed to wrap any SiFive core configuration.
The goal of the fpga-shell system is to reduce the number of wrappers to have only
one for each physical device rather than one for every combination of physical device and core configuration.

Each shell consists of Overlays which use dependency injection to create and connect interfaces in an FPGADesign to the toplevel shell module.

Most devices already have an overlay defined for them in `src/main/scala/shell[/xilinx]`.
If you're using a Xilinx device, you'll probably want to use the xilinx-specific overlay
because it defines a few things that you'd otherwise have to specify yourself.

Generally, you'll want to create a device shell that extends `Series7Shell` or `UltraScaleShell`.
Alternatively, for a Xilinx device, extend `XilinxShell` and assign to the abstract member `pllFactory` an instance of `PLLFactory`
(located in `src/main/scala/clocks/PLLFactory.scala`).

For example:

```Scala
class DeviceShell()(implicit p: Parameters) extends UltraScaleShell {
  // create Overlays
  val myperipheral = Overlay(PeripheralOverlayKey) (new PeripheralOverlay(_,_,_))
  // ...

  // assign abstract members
  val pllReset = InModuleBody { Wire(Bool()) }
  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // ensure clocks are connected
  designParameters(ClockInputOverlayKey).foreach { unused =>
    val source = unused(ClockInputOverlayParams())
    val sink = ClockSinkNode(Seq(ClockSinkParameters()))
    sink := source
  }

  // override module implementation to connect reset
  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    pllReset := reset
  }
}
```

Each peripheral device added to the shell must define an `Overlay` class, an `OverlayKey` case object, and `OverlayParams` case class.
Many devices will not need much for the `OverlayParams`. Typically, the following is sufficient:

```Scala
case class PeripheralOverlayParams(param: ParamType, controlBus: CBusType, memBus: MBusType)(implicit val p: Parameters)
case object PeripheralOverlayKey extends Field[Seq[DesignOverlay[PeripheralOverlayParams, PeripheralDeviceModule]]](Nil)
```

Modify, add, or remove `param`, `controlBus`, and `memBus` as necessary.

`PeripheralDeviceModule` is the `DesignOutput` parameter to `DesignOverlay`, which is the module interface the peripheral device specifies.
For example, the `UARTOverlayKey` has a `DesignOutput` of `TLUART`, a module to create a UART transciever hanging off of a TL bus.

For simpler devices, `DesignOutput` can be `ModuleValue[DeviceIO]`, where `DeviceIO` is a subtype of `Data` that specifies the IO of the device.
For example, the `JTAGDebugOverlayKey` has a `DesignOutput` of `ModuleValue[FPGAJTAGIO]` which is a subclass of `Bundle` with IO used by the jtag interface spec.

```Scala
class PeripheralOverlay(val params: PeripheralOverlayParams) extends IOOverlay[PeripheralTopIO, PeripheralDeviceModule] {
  // abstract member of IOOverlay
  def ioFactory = new PeripheralTopIO
  implicit val p = params.p
}
```

Most of the time, the toplevel IO will be the same as the device IO, so `PeripheralDeviceModule` can be substituted with `ModuleValue[PeripheralTopIO]`

