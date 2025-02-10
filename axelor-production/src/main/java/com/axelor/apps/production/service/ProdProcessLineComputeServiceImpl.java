package com.axelor.apps.production.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProdProcessLineComputeServiceImpl implements ProdProcessLineComputeService {

  @Override
  public BigDecimal computeLineCost(ProdProcessLine prodProcessLine, BigDecimal qtyToProduce) {
    BigDecimal maxCapacityPerCycle = prodProcessLine.getMaxCapacityPerCycle();
    WorkCenter workCenter = prodProcessLine.getWorkCenter();
    BigDecimal nbCycles = getNbCycles(maxCapacityPerCycle, qtyToProduce);
    BigDecimal durationPerCycleDecimal = prodProcessLine.getDurationPerCycleDecimal();
    BigDecimal setupDuration =
        BigDecimal.valueOf(prodProcessLine.getSetupDuration())
            .divide(
                BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    BigDecimal startingDuration =
        BigDecimal.valueOf(prodProcessLine.getStartingDuration())
            .divide(
                BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    BigDecimal endingDuration =
        BigDecimal.valueOf(prodProcessLine.getEndingDuration())
            .divide(
                BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    BigDecimal costAmount = workCenter.getCostAmount();

    BigDecimal machineDuration =
        durationPerCycleDecimal
            .multiply(nbCycles)
            .add((setupDuration.multiply(nbCycles.subtract(BigDecimal.ONE))))
            .add(startingDuration)
            .add(endingDuration);

    BigDecimal machineCostAmount =
        computeMachineCostAmount(qtyToProduce, workCenter, costAmount, machineDuration, nbCycles);
    BigDecimal humanCostAmount =
        computeHumanCostAmount(prodProcessLine, qtyToProduce, nbCycles, workCenter);

    return machineCostAmount.add(humanCostAmount);
  }

  protected BigDecimal computeHumanCostAmount(
      ProdProcessLine prodProcessLine,
      BigDecimal qtyToProduce,
      BigDecimal nbCycles,
      WorkCenter workCenter) {
    BigDecimal humanDuration = computeHumanDuration(prodProcessLine, nbCycles);

    BigDecimal hrCostAmount = workCenter.getHrCostAmount();

    BigDecimal humanCostAmount = BigDecimal.ZERO;
    int workCenterHrCostTypeSelect = workCenter.getHrCostTypeSelect();
    switch (workCenterHrCostTypeSelect) {
      case WorkCenterRepository.COST_TYPE_PER_HOUR:
        humanCostAmount = hrCostAmount.multiply(humanDuration);
        break;
      case WorkCenterRepository.COST_TYPE_PER_PIECE:
        humanCostAmount = hrCostAmount.multiply(qtyToProduce);
        break;
      default:
    }
    return humanCostAmount;
  }

  protected BigDecimal computeHumanDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles) {
    BigDecimal humanDurationDecimal = prodProcessLine.getHumanDurationDecimal();
    BigDecimal timeBeforeNextOperation =
        BigDecimal.valueOf(prodProcessLine.getTimeBeforeNextOperation())
            .divide(
                BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);

    return humanDurationDecimal
        .multiply(nbCycles)
        .add(timeBeforeNextOperation.multiply(nbCycles.subtract(BigDecimal.ONE)));
  }

  protected BigDecimal computeMachineCostAmount(
      BigDecimal qtyToProduce,
      WorkCenter workCenter,
      BigDecimal costAmount,
      BigDecimal machineDuration,
      BigDecimal nbCycles) {
    BigDecimal machineCostAmount = BigDecimal.ZERO;
    int workCenterCostTypeSelect = workCenter.getCostTypeSelect();
    switch (workCenterCostTypeSelect) {
      case WorkCenterRepository.COST_TYPE_PER_HOUR:
        machineCostAmount = costAmount.multiply(machineDuration);
        break;
      case WorkCenterRepository.COST_TYPE_PER_CYCLE:
        machineCostAmount = costAmount.multiply(nbCycles);
        break;
      case WorkCenterRepository.COST_TYPE_PER_PIECE:
        machineCostAmount = costAmount.multiply(qtyToProduce);
        break;
      default:
    }
    return machineCostAmount;
  }

  protected BigDecimal getNbCycles(BigDecimal maxCapacityPerCycle, BigDecimal qtyToProduce) {
    if (maxCapacityPerCycle.compareTo(BigDecimal.ZERO) == 0) {
      return qtyToProduce;
    } else {
      return qtyToProduce.divide(maxCapacityPerCycle, 0, RoundingMode.UP);
    }
  }
}
