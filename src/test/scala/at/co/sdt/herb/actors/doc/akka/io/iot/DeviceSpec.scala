package at.co.sdt.herb.actors.doc.akka.io.iot

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }

import akka.actor.{ ActorSystem, PoisonPill }
import akka.testkit.{ TestKit, TestProbe }

import scala.concurrent.duration._

class DeviceSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with FlatSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("AkkaQuickstartSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "A Device actor" should "reply with empty reading if no temperature is known" in {
    val probe = TestProbe()
    val deviceActor = system.actorOf(Device.props("gr1", "dev1"))

    deviceActor.tell(Device.ReadTemperature(requestId = 42), probe.ref)
    val response = probe.expectMsgType[Device.RespondTemperature]
    response.requestId should ===(42)
    response.value should ===(None)
  }

  it should "reply with latest temperature reading" in {
    val probe = TestProbe()
    val deviceActor = system.actorOf(Device.props("gr1", "dev1"))

    deviceActor.tell(Device.RecordTemperature(requestId = 1, 24.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 1))

    deviceActor.tell(Device.ReadTemperature(requestId = 2), probe.ref)
    val response1 = probe.expectMsgType[Device.RespondTemperature]
    response1.requestId should ===(2)
    response1.value should ===(Some(24.0))

    deviceActor.tell(Device.RecordTemperature(requestId = 3, 55.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 3))

    deviceActor.tell(Device.ReadTemperature(requestId = 4), probe.ref)
    val response2 = probe.expectMsgType[Device.RespondTemperature]
    response2.requestId should ===(4)
    response2.value should ===(Some(55.0))
  }

  it should "reply to registration requests" in {
    val probe = TestProbe()
    val deviceActor = system.actorOf(Device.props("gr1", "dev1"))

    deviceActor.tell(DeviceManager.RequestTrackDevice("gr1", "dev1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    probe.lastSender should ===(deviceActor)
  }

  it should "ignore wrong registration requests" in {
    val probe = TestProbe()
    val deviceActor = system.actorOf(Device.props("gr1", "dev1"))

    deviceActor.tell(DeviceManager.RequestTrackDevice("wrongGroup", "dev1"), probe.ref)
    probe.expectNoMsg(500.milliseconds)

    deviceActor.tell(DeviceManager.RequestTrackDevice("gr1", "Wrongdevice"), probe.ref)
    probe.expectNoMsg(500.milliseconds)
  }

  "A DeviceGroup" should "be able to register a device actor" in {
    val probe = TestProbe()
    val groupActor = system.actorOf(DeviceGroup.props("grp1"))

    groupActor.tell(DeviceManager.RequestTrackDevice("grp1", "dev1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor1 = probe.lastSender

    groupActor.tell(DeviceManager.RequestTrackDevice("grp1", "dev2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor2 = probe.lastSender
    deviceActor1 should !==(deviceActor2)

    // Check that the device actors are working
    deviceActor1.tell(Device.RecordTemperature(requestId = 0, 1.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 0))
    deviceActor2.tell(Device.RecordTemperature(requestId = 1, 2.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 1))
  }

  it should "ignore requests for wrong groupId" in {
    val probe = TestProbe()
    val groupActor = system.actorOf(DeviceGroup.props("grp1"))

    groupActor.tell(DeviceManager.RequestTrackDevice("wrongGroup", "dev1"), probe.ref)
    probe.expectNoMsg(500.milliseconds)
  }

  it should "return same actor for same deviceId" in {
    val probe = TestProbe()
    val groupActor = system.actorOf(DeviceGroup.props("grp1"))

    groupActor.tell(DeviceManager.RequestTrackDevice("grp1", "dev1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor1 = probe.lastSender

    groupActor.tell(DeviceManager.RequestTrackDevice("grp1", "dev1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor2 = probe.lastSender

    deviceActor1 should ===(deviceActor2)
  }

  it should "be able to list active devices" in {
    val probe = TestProbe()
    val groupActor = system.actorOf(DeviceGroup.props("grp1"))

    groupActor.tell(DeviceManager.RequestTrackDevice("grp1", "dev1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)

    groupActor.tell(DeviceManager.RequestTrackDevice("grp1", "dev2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)

    groupActor.tell(DeviceGroup.RequestDeviceList(requestId = 0), probe.ref)
    probe.expectMsg(DeviceGroup.ReplyDeviceList(requestId = 0, Set("dev1", "dev2")))
  }

  it should "be able to list active devices after one shuts down" in {
    val probe = TestProbe()
    val groupActor = system.actorOf(DeviceGroup.props("grp1"))

    groupActor.tell(DeviceManager.RequestTrackDevice("grp1", "dev1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val toShutDown = probe.lastSender

    groupActor.tell(DeviceManager.RequestTrackDevice("grp1", "dev2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)

    groupActor.tell(DeviceGroup.RequestDeviceList(requestId = 0), probe.ref)
    probe.expectMsg(DeviceGroup.ReplyDeviceList(requestId = 0, Set("dev1", "dev2")))

    probe.watch(toShutDown)
    toShutDown ! PoisonPill
    probe.expectTerminated(toShutDown)

    // using awaitAssert to retry because it might take longer for the groupActor
    // to see the Terminated, that order is undefined
    probe.awaitAssert {
      groupActor.tell(DeviceGroup.RequestDeviceList(requestId = 1), probe.ref)
      probe.expectMsg(DeviceGroup.ReplyDeviceList(requestId = 1, Set("dev2")))
    }
  }
  "A DeviceManager" should "be able to register a device actor" in {
    val probe = TestProbe()
    val deviceManager = system.actorOf(DeviceManager.props)

    deviceManager.tell(DeviceManager.RequestTrackDevice("grp1", "dev1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor1 = probe.lastSender

    deviceManager.tell(DeviceManager.RequestTrackDevice("grp1", "dev2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor2 = probe.lastSender

    deviceManager.tell(DeviceManager.RequestTrackDevice("grp2", "dev1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor3 = probe.lastSender

    deviceManager.tell(DeviceManager.RequestTrackDevice("grp2", "dev2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor4 = probe.lastSender

    deviceActor1 should !==(deviceActor2)
    deviceActor1 should !==(deviceActor3)
    deviceActor1 should !==(deviceActor4)
    deviceActor2 should !==(deviceActor3)
    deviceActor2 should !==(deviceActor4)
    deviceActor3 should !==(deviceActor4)

    // Check that the device actors are working
    deviceActor1.tell(Device.RecordTemperature(requestId = 0, 1.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 0))
    deviceActor2.tell(Device.RecordTemperature(requestId = 1, 2.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 1))
    deviceActor3.tell(Device.RecordTemperature(requestId = 2, 3.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 2))
    deviceActor4.tell(Device.RecordTemperature(requestId = 3, 4.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 3))
  }

  it should "return same actor for same deviceId" in {
    val probe = TestProbe()
    val deviceManager = system.actorOf(DeviceManager.props)

    deviceManager.tell(DeviceManager.RequestTrackDevice("grp1", "dev1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor1 = probe.lastSender

    deviceManager.tell(DeviceManager.RequestTrackDevice("grp1", "dev1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor2 = probe.lastSender

    deviceManager.tell(DeviceManager.RequestTrackDevice("grp2", "dev1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor3 = probe.lastSender

    deviceManager.tell(DeviceManager.RequestTrackDevice("grp2", "dev1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor4 = probe.lastSender

    deviceActor1 should ===(deviceActor2)
    deviceActor3 should ===(deviceActor4)
  }


}
