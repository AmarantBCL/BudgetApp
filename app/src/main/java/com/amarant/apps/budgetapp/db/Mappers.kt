package com.amarant.apps.budgetapp.db

import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.BudgetUI

object Mappers {

    private fun entityToUI(budgetDbModel: Budget): BudgetUI {
        return BudgetUI(
            budget = budgetDbModel
        )
    }

    fun entityListToUIList(list: List<Budget>): List<BudgetUI> {
        return list.map {
            BudgetUI(
                budget = it
            )
        }
    }
}