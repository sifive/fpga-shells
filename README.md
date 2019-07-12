# fpga-shells

An FPGA shell is a Chisel module designed to wrap any SiFive core configuration.
The goal of the fpga-shell system is to reduce the number of wrappers to have only
one for each physical device rather than one for every combination of physical device and core configuration.

Each shell consists of Overlays which use dependency injection to create and connect peripheral device interfaces in an FPGADesign to the toplevel shell module.

Most devices already have an overlay defined for them in `src/main/scala/shell[/xilinx]`.
If you're using a Xilinx device, you'll probably want to use the xilinx-specific overlay
because it defines a few things that you'd otherwise have to specify yourself.

Generally, you'll want to create a device shell that extends `Series7Shell` or `UltraScaleShell`.
If you need different functionality (or you're not using a Xilinx device), you can extend `Shell` and implement abstract members.
Some Microsemi devices are supported by fgpa-shells as well (and can be found in `src/main/scala/shell/microsemi`)

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

Each peripheral device to be added to the shell must define an `Overlay`, which creates the device and connects it to the toplevel shell.
In addition, in order to access the overlay, the device needs to have a `case class OverlayParams` and a `case object OverlayKey`

```Scala
case class PeripheralOverlayParams()(implicit val p: Parameters)
case object PeripheralOverlayKey extends Field[Seq[DesignOverlay[PeripheralOverlayParams, PeripheralDesignOutput]]](Nil)
```

If the device is parameterizable, then each parameter for the device creation can be passed to the `PeripheralOverlayParams` constructor by adding a field for said parameter.
Typically, devices are connected to a TileLink bus for processor control, so `PeripheralDesignOutput` can usually be substituted with `TLInwardNode`.

The `Overlay` extends `IOOverlay` which is paramerized by the device's `IO` (in this case `PeripheralDeviceIO` is a subtype of `Data` and is a port specification for the peripheral device)
and `DesignOutput`.

```Scala
abstract class AbstractPeripheralOverlay(val params: PeripheralOverlayParams)
  extends IOOverlay[PeripheralDeviceIO, PeripheralDesignOutput]
{
  // assign abstract member p (used to access overlays with their key)
  // e.g. p(PeripheralOverlayKey) will return a Seq[DesignOverlay[PeripheralOverlayParams, PeripheralDesignOutput]]
  implicit val p = params.p
}
```

Continuing our example with a `DeviceShell` shell, the actual overlay is constructed by extending our abstract `PeripheralOverlay`
```Scala
class ConcretePeripheralOverlay(val shell: DeviceShell, val name: String, params: PeripheralOverlayParams)
  extends AbstractPeripheralOverlay(params)
{
  val device = LazyModule(new PeripheralDevice(PeripheralDeviceParams(???))) // if your peripheral device isn't parameterizable, then it'll have an empty constructor

  def ioFactory = new PeripheralDeviceIO // ioFactory defines interface of val io
  val designOutput = device.node

  // this is where "code-injection" starts
  val ioSource = BundleBridgeSource(() => device.module.io.cloneType) // create a bridge between device (source) and shell (sink)
  val ioSink = shell { ioSource.makeSink() }

  InModuleBody { ioSource.bundle <> device.module.io }

  shell { InModuleBody {
    val port = ioSink.bundle

    io <> port // io is the bundle of shell-level IO as specified by ioFactory
  } }
}
```

The actual device implementation (where the device's functionality is defined) will be something like this:
```Scala
case class PeripheralDeviceParams(param1: Param1Type, ???) // only necessary if your device is parameterizable
class PeripheralDevice(c: PeripheralDeviceParams)(implicit p: Parameters) extends LazyModule {
  
  val node: PeripheralDesignOutput = ???

  // device implementation
  lazy val module = new LazyModuleImp(this) {
    val io = ???
    ???
  }
}
```
