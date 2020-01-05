package fr.mipn.parc.actors

import akka.actor.{Actor, ActorLogging, Props}
import io.swagger.server.model.{Error, PlaceType, PricingPlan}
import org.joda.time.{DateTime, Hours, Minutes}


object FeeCalculator {

  val placeTypes = List(PlaceType(1, "voitures", "desc de type voitures"),
    PlaceType(2, "velo", "desc de type velo"))

  var plans: Map[String, PricingPlan] = Map(
    "Starter" -> PricingPlan(
      "Starter",
      2.50d,
      1,
      List(1, 2),
      3.0d
    ),
    "Premium" -> PricingPlan(
      "Premium",
      3.5d,
      5,
      List(1),
      1.0d
    )
  ).withDefaultValue(null)

  case object GetPricingPlans

  case class CheckPricingExists(pricingPlanName: String)

  case class CalculateFee(start: DateTime, pricingName: String)

  def apply(): Props = Props(new FeeCalculator())
}

case class FeeCalculator() extends Actor with ActorLogging {

  import FeeCalculator._

  override def receive: Receive = {

    case GetPricingPlans =>
      val plansList = plans.values.toList
      sender ! Right(plansList)

    case check: CheckPricingExists =>
      val exists = plans(check.pricingPlanName) != null
      sender ! Right(exists)

    case calculateFee: CalculateFee =>

      val chosenPlan = plans.find((p) => p._1 == calculateFee.pricingName).orNull._2
      if (chosenPlan == null) {
        sender ! Error("plan not found")
      }
      val minTime = chosenPlan.minNumberOfHours
      val hoursBetween = Minutes.minutesBetween(calculateFee.start, DateTime.now).getMinutes / 60


      val (normalTime, extraTime) = if (hoursBetween <= minTime) {
        (hoursBetween, 0)
      } else {
        (minTime, hoursBetween - minTime)
      }

      val fee: Double = (normalTime * chosenPlan.pricePerHour + extraTime * chosenPlan.extraTimeFine)

      sender ! Some(fee)

    case _@msg => sender ! s"I recieved the msg $msg"
  }
}
