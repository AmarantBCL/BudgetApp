package com.amarant.apps.budgetapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.amarant.apps.budgetapp.R

class HistoryAdapter(
    context: Context,
    private val items: MutableList<String>,
    private val onDeleteClickListener: (String) -> Unit // Лямбда для обработки удаления
) : ArrayAdapter<String>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Используем LayoutInflater для создания нашего макета
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_history, parent, false)

        val historyItem = getItem(position) ?: return view

        val textView = view.findViewById<TextView>(R.id.tv_history)
        val deleteButton = view.findViewById<ImageButton>(R.id.btn_delete)

        // 1. Установка текста подсказки
        textView.text = historyItem

        // 2. Обработка клика по кнопке "X"
        deleteButton.setOnClickListener {
            // Вызываем внешний обработчик
            onDeleteClickListener.invoke(historyItem)
        }

        // 3. (Важно!) Обработка клика по самой строке
        // Если пользователь нажимает на текст, а не на кнопку,
        // должно произойти автозаполнение.
        textView.setOnClickListener {
            // Имитация клика на всю строку для автозаполнения
            (parent as? ListView)?.performItemClick(
                view,
                position,
                getItemId(position)
            )
        }

        return view
    }
}