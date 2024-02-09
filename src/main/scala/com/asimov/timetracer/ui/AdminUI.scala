package com.asimov.timetracer.ui

import com.asimov.timetracer.ui.ReportsUI.getClass
import com.asimov.timetracer.{Log, MyButton, MyLabel, MySQL, showMessage}
import com.github.lgooddatepicker.tableeditors.{DateTableEditor, DateTimeTableEditor}
import com.github.lgooddatepicker.zinternaltools.InternalUtilities

import java.awt.Color
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import java.util
import java.util.Date
import javax.swing.{Box, ImageIcon, ListSelectionModel}
import javax.swing.event.{TableModelEvent, TableModelListener}
import javax.swing.table.DefaultTableModel
import scala.collection.mutable
import scala.swing.Orientation.Vertical
import scala.swing.ScrollPane.BarPolicy
import scala.swing.ScrollPane.BarPolicy.Always
import scala.swing.Table.AutoResizeMode
import scala.swing.event.{ButtonClicked, KeyReleased}
import scala.swing.event.Key.{Delete, Down}
import scala.swing.event.Key.Modifier.Control
import scala.swing.{BoxPanel, Button, ButtonGroup, Dialog, Dimension, FlowPanel, Label, RadioButton, ScrollPane, Table}

case class UpdateData(tableName: String, colName: String, colValue: String,
                      previoiusValue: String, idCol: String, IdVal: String)
case class AppendData(tableName: String, columns: mutable.HashMap[String, String])
case class DeleteData(tableName: String, previousValue: String, idCol: String, IdVal: String)

object AdminUI extends Dialog {
  private var appending: Option[AppendData] = None
  private var deleting: Option[DeleteData] = None
  private val updates: mutable.ListBuffer[UpdateData] = new mutable.ListBuffer()
  private val updatesPending = new MyLabel(" ") {
    preferredSize = new Dimension(100, 15)
    foreground = Color.red
  }

  private class MyTableModel extends DefaultTableModel {
    private var lastValue: Option[Object] = None

    addTableModelListener((e: TableModelEvent) => {
      appending match {
        case Some(appendData) => // Append Mode
          if (e.getColumn >= 0) {
            val colName = getColumnName(e.getColumn)
            val colValue = getValueAt(e.getFirstRow, e.getColumn).toString
            appendData.columns += (colName -> s"'$colValue'")
          }
        case None => // Update Mode
          if (e.getFirstRow >= 0 && e.getColumn >= 0) {
            if (tableTitle.text != "Logs") {
              if (lastValue.getOrElse("").toString != getValueAt(e.getFirstRow, e.getColumn).toString) {
                updates += UpdateData(tableTitle.text,
                  getColumnName(e.getColumn),
                  getValueAt(e.getFirstRow, e.getColumn).toString,
                  s"${getColumnName(e.getColumn)}=$lastValue",
                  getColumnName(0),
                  getValueAt(e.getFirstRow, 0).toString)
                updatesPending.text = "Updates Pending!"
                enableTableButtons(false)
              }
            }
          }
      }
    })

    override def setValueAt(value: Object, row: Int, col: Int): Unit = {
      lastValue = Some(getValueAt(row, col))
      super.setValueAt(value, row, col)
    }

    override def isCellEditable(row: Int, column: Int): Boolean = this.getColumnName(column) match {
        case str if str.startsWith("ID") => false
        case str if str.endsWith("Timestamp") => false
        case "EmployeeID" if appending.isEmpty => false
        case "InsertUser" => false
        case "ModifyUser" => false
        case "Query" => false
        case "PreviousState" => false
        case _ => true
      }
  }

  private val table = new Table() {
    peer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    peer.setPreferredScrollableViewportSize(new Dimension(750, 200))
    peer.setFillsViewportHeight(true)
    model = new MyTableModel
    autoResizeMode = AutoResizeMode.Off
    showGrid = true
    InternalUtilities.setDefaultTableEditorsClicks(peer, 2)
    private val DateTimeRenderer = new DateTimeTableEditor {
      getDatePickerSettings.setFormatForDatesCommonEra("dd/MM/yyyy")
      getTimePickerSettings.setFormatForDisplayTime("HH:mm")
    }
    peer.setDefaultRenderer(classOf[LocalDateTime], DateTimeRenderer)
    private val dateTimeEditor = new DateTimeTableEditor {
      clickCountToEdit = 2
      getDatePickerSettings.setFormatForDatesCommonEra("dd/MM/yyyy")
      getTimePickerSettings.setFormatForDisplayTime("HH:mm")
    }
    peer.setDefaultEditor(classOf[LocalDateTime], dateTimeEditor)
    private val DateRenderer = new DateTableEditor {
      getDatePickerSettings.setFormatForDatesCommonEra("dd/MM/yyyy")
    }
    peer.setDefaultRenderer(classOf[LocalDate], DateRenderer)
    private val dateEditor = new DateTableEditor {
      getDatePickerSettings.setFormatForDatesCommonEra("dd/MM/yyyy")
      clickCountToEdit = 2
    }
    peer.setDefaultEditor(classOf[LocalDate], dateEditor)
    listenTo(keys)
    reactions += {
      case KeyReleased(table, Down, _, _) => appendRow()
      case KeyReleased(table, Delete, Control, _) => deleteRow()
    }
  }

  private val tableTitle = MyLabel("Times")
  private val tableTitleContainer = new FlowPanel() {
    contents += tableTitle
  }
  private val scrollPane: ScrollPane = new ScrollPane(table) {
    horizontalScrollBarPolicy = Always
    verticalScrollBarPolicy = Always
  }
  private val scrolPaneContainer = new FlowPanel() {
    peer.add(Box.createHorizontalStrut(5))
    contents += scrollPane
    peer.add(Box.createHorizontalStrut(5))
  }
  private val topPanel: BoxPanel = new BoxPanel(Vertical) {
    peer.add(Box.createVerticalStrut(5))
    contents += tableTitleContainer
    peer.add(Box.createVerticalStrut(5))
    contents += scrolPaneContainer
  }

  private val timesRdo: RadioButton = new RadioButton("Times")
  private val empsRdo: RadioButton = new RadioButton("Employees")
  private val usersRdo: RadioButton = new RadioButton("Users")
  private val projsRdo: RadioButton = new RadioButton("Projects")
  private val rolesRdo: RadioButton = new RadioButton("Roles")
  private val logsRdo: RadioButton = new RadioButton("Logs")
  private val buttonGroup: ButtonGroup = new ButtonGroup(timesRdo, empsRdo, usersRdo, projsRdo, rolesRdo, logsRdo)
  private val radioPanel: BoxPanel = new BoxPanel(Vertical) {
    val between = 2
    val around = 10
    peer.add(Box.createVerticalStrut(around))
    contents += new MyLabel("Tables")
    peer.add(Box.createVerticalStrut(between))
    contents += timesRdo
    peer.add(Box.createVerticalStrut(between))
    contents += empsRdo
    peer.add(Box.createVerticalStrut(between))
    contents += usersRdo
    peer.add(Box.createVerticalStrut(between))
    contents += projsRdo
    peer.add(Box.createVerticalStrut(between))
    contents += rolesRdo
    peer.add(Box.createVerticalStrut(between))
    contents += logsRdo
    peer.add(Box.createVerticalStrut(around))
  }

  private val applyBtn: Button = new MyButton("Apply")
  private val undoBtn: Button = new MyButton("Undo")
  private val buttonPanel1: BoxPanel = new BoxPanel(Vertical) {
    peer.add(Box.createVerticalStrut(30))
    contents += applyBtn
    peer.add(Box.createVerticalStrut(15))
    contents += undoBtn
    peer.add(Box.createVerticalStrut(15))
    contents += updatesPending
  }

  private val logo = new Label() {
    icon = new ImageIcon(getClass.getResource("/logo.png"))
  }

  private val chPwdBtn: Button = new MyButton("Ch. Pwd")
  private val resetPwd: Button = new MyButton("Rst  Pwd")
  private val buttonPanel2: BoxPanel = new BoxPanel(Vertical) {
    peer.add(Box.createVerticalStrut(28))
    contents += chPwdBtn
    peer.add(Box.createVerticalStrut(15))
    contents += resetPwd
    peer.add(Box.createVerticalStrut(29))
  }

  private val reportBtn: Button = new MyButton("Reports")
  private val closeBtn: Button = new MyButton("Close")
  private val buttonPanel3: BoxPanel = new BoxPanel(Vertical) {
    peer.add(Box.createVerticalStrut(28))
    contents += reportBtn
    peer.add(Box.createVerticalStrut(15))
    contents += closeBtn
    peer.add(Box.createVerticalStrut(29))
  }

  private val bottomPanel: FlowPanel = new FlowPanel(FlowPanel.Alignment.Left)() {
    peer.add(Box.createHorizontalStrut(35))
    contents += logo
    peer.add(Box.createHorizontalStrut(50))
    contents += radioPanel
    peer.add(Box.createHorizontalStrut(40))
    contents += buttonPanel1
    peer.add(Box.createHorizontalStrut(30))
    contents += buttonPanel2
    peer.add(Box.createHorizontalStrut(30))
    contents += buttonPanel3
  }

  private val adminPanel: BoxPanel = new BoxPanel(Vertical) {
    contents += topPanel
    contents += bottomPanel
  }

  override def closeOperation(): Unit = System.exit(0)

  def apply(): Unit = show()

  private def readTable(query: String, title: String): Unit = {
    if(db.connection.nonEmpty) {
      val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val rs: ResultSet = db.connection.get.createStatement().executeQuery(query)
      val meta = rs.getMetaData
      val colCount = meta.getColumnCount
      val columnNames = (1 to colCount).toArray.map(meta.getColumnName).asInstanceOf[Array[AnyRef]]
      val data = Iterator.from(0).takeWhile(_ => rs.next())
        .map(_ => (1 to colCount).toArray.map(c => {
            meta.getColumnTypeName(c) match {
              case "BIT" => rs.getBoolean(c)
              case "DATE" => LocalDate.parse(rs.getString(c), dateFormatter)
              case "DATETIME" => LocalDateTime.parse(rs.getString(c), dateTimeFormatter)
              case _ => rs.getString(c)
            }})).toArray.asInstanceOf[Array[Array[AnyRef]]]
      val model: DefaultTableModel = table.model.asInstanceOf[DefaultTableModel]
      table.model = model
      model.setDataVector(data, columnNames)
      for (i <- 0 until table.model.getColumnCount) {
        val col = table.peer.getColumnModel.getColumn(i)
        model.getColumnName(i) match {
          case s"${s}Time${_}" => if (s != "ID") col.setPreferredWidth(210)
          case "In" =>                           col.setPreferredWidth(30)
          case "BirthDate" =>                    col.setPreferredWidth(100)
          case "SocialSecurityNum" =>            col.setPreferredWidth(175)
          case "Residence" =>                    col.setPreferredWidth(200)
          case "Query" =>                        col.setPreferredWidth(280)
          case "PreviousState" =>                col.setPreferredWidth(280)
          case _ =>
        }
      }
      tableTitle.text = title
    }
  }

  private def enableTableButtons(enable: Boolean): Unit = {
    timesRdo.enabled = enable
    empsRdo.enabled = enable
    usersRdo.enabled = enable
    projsRdo.enabled = enable
    rolesRdo.enabled = enable
    logsRdo.enabled = enable
  }

  private def appendRow(): Unit = {
    appending match {
      case Some(appendData) =>
      case None =>
        appending = Some(AppendData(tableTitle.text, mutable.HashMap()))
        val row: util.Vector[String] = new util.Vector[String](table.model.getColumnCount)
        table.model.asInstanceOf[DefaultTableModel].addRow(row)
        enableTableButtons(false)
        updatesPending.text = "Append Pending!"
    }
  }

  private def deleteRow(): Unit = {
    val tableName = tableTitle.text
    if (tableName != "Logs") {
      val row = table.peer.getSelectedRow
      val previousValue = (0 until table.model.getColumnCount)
        .map(i => s"${table.model.getColumnName(i)}=${table.model.getValueAt(row, i)}")
        .mkString(", ")
      val idCol = table.model.getColumnName(0)
      val idVal = table.model.getValueAt(row, 0).toString
      if(table.peer.isEditing) table.peer.getCellEditor().stopCellEditing()
      table.model.asInstanceOf[DefaultTableModel].removeRow(row)
      updatesPending.text = "Delete Pending!"
      enableTableButtons(false)
      deleting = Some(DeleteData(tableName, previousValue, idCol, idVal))
    }
  }

  private def applyChange(): Unit = {
    if (db.connection.nonEmpty) {
      if(updates.nonEmpty && deleting.isEmpty){
        val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val oks: mutable.ListBuffer[Boolean] = mutable.ListBuffer()
        for (update <- updates) {
          val timestamp = dateFormat.format(new Date())
          val mod = if (update.tableName == "Times") s", ModifyUser = 'Admin', ModifyTimestamp = '$timestamp' " else ""
          val query = s"UPDATE `TimeTracer`.`${update.tableName}` SET `${update.colName}` = ? $mod WHERE `${update.idCol}` = ?"
          val loggedQuery = s"UPDATE `TimeTracer`.`${update.tableName}` " +
            s"SET `${update.colName}` = '${update.colValue}' $mod WHERE `${update.idCol}` = '${update.IdVal}'"
          val statement = db.connection.get.prepareStatement(query)
          update.colValue match {
            case "true" => statement.setBoolean(1, true)
            case "false" => statement.setBoolean(1, false)
            case _ => statement.setString(1, update.colValue)
          }
          statement.setString(2, update.IdVal)
          val ok = statement.executeUpdate() > 0
          if(ok) Log(loggedQuery, update.previoiusValue)
          oks += ok
        }
        if (oks.exists(!_)) showMessage(this.peer, "Problem Updating DataBase", "Errors", true)
        else showMessage(this.peer, "Update Correctly Applied", "Updated", false)
      }
      if(appending.nonEmpty) {
        val tbl = appending.get.tableName
        val fields = appending.get.columns.keys.mkString("(`", "`, `", "`)")
        val values = appending.get.columns.values.mkString("(", ", ", ")")
        val query = s"INSERT INTO `TimeTracer`.`$tbl` $fields VALUES $values"
        if(db.connection.get.createStatement().executeUpdate(query) > 0) {
          Log(query, "")
          showMessage(this.peer, "Append Correctly Applied", "Updated", false)
        } else showMessage(this.peer, "Problem Updating DataBase", "Errors", true)
      }
      if(deleting.nonEmpty) {
        val tableName = deleting.get.tableName
        val idCol = deleting.get.idCol
        val idVal = deleting.get.IdVal
        val query = s"DELETE FROM `TimeTracer`.`$tableName` WHERE `$idCol` = '$idVal'"
        if (db.connection.get.createStatement().executeUpdate(query) > 0) {
          Log(query, deleting.get.previousValue)
          showMessage(this.peer, "Delete Correctly Applied", "Updated", false)
        } else showMessage(this.peer, "Problem Updating DataBase", "Errors", true)
      }
      if(updates.isEmpty && appending.isEmpty && deleting.isEmpty)
        showMessage(this.peer, "Nothing to Update", "Errors", true)
      else resetChanges()
    }
  }

  private def refreshTable(): Unit = {
    buttonGroup.selected match {
      case Some(button) => button.doClick()
      case _ =>
    }
  }

  private def resetChanges(): Unit = {
    updatesPending.text = " "
    enableTableButtons(true)
    refreshTable()
    updates.clear()
    appending = None
  }

  private def resetPwd4User(): Unit = {
    if(tableTitle.text == "Users") {
      if (db.connection.nonEmpty) {
        val user = table.model.getValueAt(table.peer.getSelectedRow, 0).toString
        val query = "UPDATE `TimeTracer`.`Users` SET `Password` = NULL WHERE `UserName` = ?"
        val statement = db.connection.get.prepareStatement(query)
        statement.setString(1, user)
        if (statement.executeUpdate() > 0) {
          showMessage(this.peer, "Correctly Reset Password", "Reset Password", false)
        } else showMessage(this.peer, "Problem Resetting Password", "Reset Password", true)
      }
    } else showMessage(this.peer, "Please Select User in Users Table First", "Select User", false)
  }

  private def show(): Unit = visible = true

  private def hide(): Unit = visible = false

  private val db = MySQL
  title = "Time Tracer - Admin"
  contents = adminPanel
  listenTo(timesRdo, empsRdo, usersRdo, projsRdo, rolesRdo, logsRdo,
    applyBtn, undoBtn, closeBtn, chPwdBtn, resetPwd, reportBtn)
  reactions += {
    case ButtonClicked(`timesRdo`) => readTable("SELECT * FROM `TimeTracer`.`Times` ORDER BY `PunchedTime` DESC", "Times")
    case ButtonClicked(`empsRdo`) => readTable("SELECT * FROM `TimeTracer`.`Employees`", "Employees")
    case ButtonClicked(`usersRdo`) => readTable("SELECT `UserName`, `RoleID`, `EmployeeID` FROM `TimeTracer`.`Users`", "Users")
    case ButtonClicked(`projsRdo`) => readTable("SELECT * FROM `TimeTracer`.`Projects`", "Projects")
    case ButtonClicked(`rolesRdo`) => readTable("SELECT * FROM `TimeTracer`.`Roles`", "Roles")
    case ButtonClicked(`logsRdo`) => readTable("SELECT * FROM `TimeTracer`.`Log` ORDER BY `IDLog` DESC", "Logs")
    case ButtonClicked(`applyBtn`) => applyChange()
    case ButtonClicked(`undoBtn`) => resetChanges()
    case ButtonClicked(`closeBtn`) => System.exit(0)
    case ButtonClicked(`resetPwd`) => resetPwd4User()
    case ButtonClicked(`chPwdBtn`) =>
      hide()
      ChangePasswordUI("Admin", 1, 0, false)
    case ButtonClicked(`reportBtn`) =>
      hide()
      ReportsUI()
  }
  timesRdo.doClick()
  pack()
  centerOnScreen()
}
