package com.example.fitgenius.utils

import com.example.fitgenius.data.WeeklyNutrition
import com.example.fitgenius.data.WeeklyWorkout
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream

class ExcelExportService {

    fun generateExcel(
        workout: WeeklyWorkout?,
        diet: WeeklyNutrition?,
        outputStream: OutputStream
    ) {
        val workbook = XSSFWorkbook()

        // Styles
        val headerStyle = createHeaderStyle(workbook)
        val contentStyle = createContentStyle(workbook)
        val mergedStyle = createMergedContentStyle(workbook)

        workout?.let {
            val sheet = workbook.createSheet("Rutina de Entrenamiento")
            createWorkoutSheet(sheet, it, headerStyle, contentStyle, mergedStyle)
        }

        diet?.let {
            val sheet = workbook.createSheet("Plan Nutricional")
            createDietSheet(sheet, it, headerStyle, contentStyle, mergedStyle)
        }

        workbook.write(outputStream)
        workbook.close()
    }

    private fun createHeaderStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont().apply {
            bold = true
            color = IndexedColors.WHITE.index
            fontHeightInPoints = 12.toShort()
        }
        style.setFont(font)
        style.fillForegroundColor = IndexedColors.DARK_GREEN.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        style.borderBottom = BorderStyle.THIN
        style.borderTop = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        return style
    }

    private fun createContentStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            wrapText = true
            verticalAlignment = VerticalAlignment.TOP
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
    }

    private fun createMergedContentStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            wrapText = true
            verticalAlignment = VerticalAlignment.CENTER
            alignment = HorizontalAlignment.LEFT
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
    }

    private fun createWorkoutSheet(sheet: Sheet, workout: WeeklyWorkout, headerStyle: CellStyle, contentStyle: CellStyle, mergedStyle: CellStyle) {
        val headers = listOf("Día", "Nombre Rutina", "Ejercicio", "Series", "Repeticiones", "Descanso", "Observaciones")
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, title ->
            headerRow.createCell(index).apply { 
                setCellValue(title)
                cellStyle = headerStyle 
            }
        }

        var rowNum = 1
        workout.week.forEach { dayWorkout ->
            val firstRowForDay = rowNum
            if (dayWorkout.exercises.isEmpty()) {
                val row = sheet.createRow(rowNum++)
                row.createCell(0).setCellValue(dayWorkout.day)
                row.createCell(1).setCellValue("Descanso")
                (0 until headers.size).forEach { i -> row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).cellStyle = contentStyle }
                sheet.addMergedRegion(CellRangeAddress(firstRowForDay, firstRowForDay, 1, headers.size - 1))
                row.getCell(1).cellStyle = mergedStyle // Center the "Descanso" text
            } else {
                dayWorkout.exercises.forEach { exercise ->
                    val row = sheet.createRow(rowNum++)
                    row.createCell(0).setCellValue(dayWorkout.day)
                    row.createCell(1).setCellValue(dayWorkout.name)
                    row.createCell(2).setCellValue(exercise.name)
                    row.createCell(3).setCellValue(exercise.series)
                    row.createCell(4).setCellValue(exercise.reps)
                    row.createCell(5).setCellValue(exercise.rest)
                    row.createCell(6).setCellValue(exercise.observations)
                    (0 until headers.size).forEach { i -> row.getCell(i).cellStyle = contentStyle }
                }
                val lastRowForDay = rowNum - 1
                if (firstRowForDay < lastRowForDay) {
                    sheet.addMergedRegion(CellRangeAddress(firstRowForDay, lastRowForDay, 0, 0))
                    sheet.addMergedRegion(CellRangeAddress(firstRowForDay, lastRowForDay, 1, 1))
                    sheet.getRow(firstRowForDay).getCell(0).cellStyle = mergedStyle
                    sheet.getRow(firstRowForDay).getCell(1).cellStyle = mergedStyle
                }
            }
        }
        
        sheet.setColumnWidth(0, 15 * 256)
        sheet.setColumnWidth(1, 30 * 256)
        sheet.setColumnWidth(2, 35 * 256)
        sheet.setColumnWidth(3, 15 * 256)
        sheet.setColumnWidth(4, 15 * 256)
        sheet.setColumnWidth(5, 15 * 256)
        sheet.setColumnWidth(6, 40 * 256)
    }

    private fun createDietSheet(sheet: Sheet, diet: WeeklyNutrition, headerStyle: CellStyle, contentStyle: CellStyle, mergedStyle: CellStyle) {
        val headers = listOf("Día", "Comida", "Hora", "Alimentos", "Calorías", "Macros (P/C/G)")
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, title ->
            headerRow.createCell(index).apply { 
                setCellValue(title)
                cellStyle = headerStyle 
            }
        }

        var rowNum = 1
        diet.week.forEach { dayDiet ->
            if (dayDiet.meals.isNotEmpty()) {
                val firstRowForDay = rowNum
                dayDiet.meals.forEach { meal ->
                    val row = sheet.createRow(rowNum++)
                    row.createCell(0).setCellValue(dayDiet.day)
                    row.createCell(1).setCellValue(meal.name)
                    row.createCell(2).setCellValue(meal.time)
                    row.createCell(3).setCellValue(meal.foods.joinToString("\n") { "- $it" })
                    row.createCell(4).setCellValue(meal.calories)
                    val macros = "P: ${meal.macros.protein}, C: ${meal.macros.carbs}, G: ${meal.macros.fats}"
                    row.createCell(5).setCellValue(macros)
                    (0 until headers.size).forEach { i -> row.getCell(i).cellStyle = contentStyle }
                }
                val lastRowForDay = rowNum - 1
                if (firstRowForDay < lastRowForDay) {
                    sheet.addMergedRegion(CellRangeAddress(firstRowForDay, lastRowForDay, 0, 0))
                    sheet.getRow(firstRowForDay).getCell(0).cellStyle = mergedStyle
                }
            }
        }
        
        sheet.setColumnWidth(0, 15 * 256)
        sheet.setColumnWidth(1, 20 * 256)
        sheet.setColumnWidth(2, 15 * 256)
        sheet.setColumnWidth(3, 50 * 256)
        sheet.setColumnWidth(4, 15 * 256)
        sheet.setColumnWidth(5, 30 * 256)
    }
}
