package com.asimov.timetracer.ui

import com.asimov.timetracer.{MyLabel, MySQL, showMessage}

import java.awt.Color
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util
import java.util.Date
import javax.swing.{Box, ImageIcon, ListSelectionModel}
import javax.swing.event.{TableModelEvent, TableModelListener}
import javax.swing.table.DefaultTableModel
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.swing.Orientation.Vertical
import scala.swing.Table.AutoResizeMode
import scala.swing.event.{ButtonClicked, KeyReleased}
import scala.swing.event.Key.{Delete, Down}
import scala.swing.event.Key.Modifier.Control
import scala.swing.{BoxPanel, Button, ButtonGroup, Dialog, Dimension, FlowPanel, Label, RadioButton, ScrollPane, Table}

case class UpdateData(tableName: String, colName: String, colValue: String, idCol: String, IdVal: String)
case class AppendData(tableName: String, columns: mutable.HashMap[String, String])
case class DeleteData(tableName: String, idCol: String, IdVal: String)

object AdminUI extends Dialog {

  private var appending: Option[AppendData] = None
  private var deleting: Option[DeleteData] = None
  private val updates: ListBuffer[UpdateData] = new ListBuffer()
  private val updatesPending = MyLabel(" ")
  updatesPending.foreground = Color.red

  private val table = new Table() {
    peer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    peer.setPreferredScrollableViewportSize(new Dimension(750, 200))
    peer.setFillsViewportHeight(true)
    model = new DefaultTableModel()
    autoResizeMode = AutoResizeMode.Off
    showGrid = true
    model.addTableModelListener((e: TableModelEvent) => {
      appending match {
        case Some(appendData) =>   // Append Mode
          if (e.getColumn >= 0) {
            val colName = model.getColumnName(e.getColumn)
            val colValue = model.getValueAt(e.getFirstRow, e.getColumn).toString
            appendData.columns += (colName -> s"'$colValue'")
          }
        case None =>               // Update Mode
          if (e.getFirstRow >= 0 && e.getColumn >= 0) {
          if (tableTitle.text != "Logs") {
            updates += UpdateData(tableTitle.text,
              model.getColumnName(e.getColumn),
              model.getValueAt(e.getFirstRow, e.getColumn).toString,
              model.getColumnName(0),
              model.getValueAt(e.getFirstRow, 0).toString)
            updatesPending.text = "Updates Pending!"
            enableTableButtons(false)
          }
        }
      }
    })
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
  private val scrollPane: ScrollPane = new ScrollPane(table)
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
  private val empsRdo: RadioButton = new RadioButton("Empolyees")
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

  private val applyBtn: Button = new Button("Apply")
  private val undoBtn: Button = new Button("Undo")
  private val buttonPanel1: BoxPanel = new BoxPanel(Vertical) {
    contents += updatesPending
    peer.add(Box.createVerticalStrut(15))
    contents += applyBtn
    peer.add(Box.createVerticalStrut(15))
    contents += undoBtn
  }

  private val logo = new Label() {
    icon = new ImageIcon(getClass.getResource("/logo.png"))
  }

  private val closeBtn: Button = new Button("Close")
  private val chPwdBtn: Button = new Button("Ch. Pwd")
  private val buttonPanel2: BoxPanel = new BoxPanel(Vertical) {
    peer.add(Box.createVerticalStrut(30))
    contents += chPwdBtn
    peer.add(Box.createVerticalStrut(15))
    contents += closeBtn
  }

  private val bottomPanel: FlowPanel = new FlowPanel(FlowPanel.Alignment.Left)() {
    peer.add(Box.createHorizontalStrut(10))
    contents += radioPanel
    peer.add(Box.createHorizontalStrut(15))
    contents += buttonPanel1
    peer.add(Box.createHorizontalStrut(100))
    contents += logo
    peer.add(Box.createHorizontalStrut(100))
    contents += buttonPanel2
  }

  private val adminPanel: BoxPanel = new BoxPanel(Vertical) {
    contents += topPanel
    contents += bottomPanel
  }

  override def closeOperation(): Unit = System.exit(0)

  def apply(): Unit = show()

  private def readTable(query: String, title: String): Unit = {
    if(db.connection.nonEmpty) {
      val rs: ResultSet = db.connection.get.createStatement().executeQuery(query)
      val meta = rs.getMetaData
      val colCount = meta.getColumnCount
      val columnNames = (1 to colCount).toArray.map(meta.getColumnName)
      val data: Array[Array[String]] = Iterator.from(0).takeWhile(_ => rs.next())
        .map(_ => (1 to colCount).toArray.map(rs.getString))
        .toArray
      val model: DefaultTableModel = table.model.asInstanceOf[DefaultTableModel]
      model.setDataVector(data.asInstanceOf[Array[Array[AnyRef]]], columnNames.asInstanceOf[Array[AnyRef]])
      for (i <- 0 until table.model.getColumnCount) {
        table.model.getColumnName(i) match {
          case "PunchedTime" => table.peer.getColumnModel.getColumn(i).setPreferredWidth(115)
          case "InsertTimestamp" => table.peer.getColumnModel.getColumn(i).setPreferredWidth(115)
          case "ModifyTimestamp" => table.peer.getColumnModel.getColumn(i).setPreferredWidth(115)
          case "In" => table.peer.getColumnModel.getColumn(i).setPreferredWidth(30)
          case "SocialSecurityNum" => table.peer.getColumnModel.getColumn(i).setPreferredWidth(175)
          case "Residence" => table.peer.getColumnModel.getColumn(i).setPreferredWidth(200)
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
        updatesPending.text = "Append Operation Pending!"
    }
  }

  private def deleteRow(): Unit = {
    val tableName = tableTitle.text
    val row = table.peer.getEditingRow
    val idCol = table.model.getColumnName(0)
    val idVal = table.model.getValueAt(row, 0).toString
    table.peer.getCellEditor().stopCellEditing()
    table.model.asInstanceOf[DefaultTableModel].removeRow(row)
    updatesPending.text = "Delete Operation Pending!"
    deleting = Some(DeleteData(tableName, idCol, idVal))
  }

  private def applyChange(): Unit = {
    if (db.connection.nonEmpty) {
      if(updates.nonEmpty && deleting.isEmpty){
        val oks: ListBuffer[Boolean] = ListBuffer()
        for (update <- updates) {
          val timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())
          val mod = if (update.tableName == "Times") s", ModifyUser = 'Admin', ModifyTimestamp = '$timestamp' " else ""
          val query = s"UPDATE TimeTracer.${update.tableName} SET ${update.colName} = ? $mod WHERE ${update.idCol} = ?"
          val statement = db.connection.get.prepareStatement(query)
          statement.setString(1, update.colValue)
          statement.setString(2, update.IdVal)
          oks += (statement.executeUpdate() > 0)
        }
        if (oks.exists(!_)) showMessage(this.peer, "Problem Updating DataBase", "Errors", true)
        else showMessage(this.peer, "Update Correctly Applied", "Updated", false)
      }
      if(appending.nonEmpty) {
        val tbl = appending.get.tableName
        val fields = appending.get.columns.keys.mkString("(", ", ", ")")
        val values = appending.get.columns.values.mkString("(", ", ", ")")
        val query = s"INSERT INTO TimeTracer.$tbl $fields VALUES $values"
        if(db.connection.get.createStatement().executeUpdate(query) > 0)
          showMessage(this.peer, "Append Correctly Applied", "Updated", false)
        else showMessage(this.peer, "Problem Updating DataBase", "Errors", true)
      }
      if(deleting.nonEmpty) {
        val tableName = deleting.get.tableName
        val idCol = deleting.get.idCol
        val idVal = deleting.get.IdVal
        val query = s"DELETE FROM TimeTracer.$tableName WHERE $idCol = '$idVal'"
        if (db.connection.get.createStatement().executeUpdate(query) > 0)
          showMessage(this.peer, "Delete Correctly Applied", "Updated", false)
        else showMessage(this.peer, "Problem Updating DataBase", "Errors", true)
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
    refreshTable()
    enableTableButtons(true)
    updates.clear()
    appending = None
  }

  private def show(): Unit = visible = true

  private def hide(): Unit = visible = false

  private val db = MySQL
  title = "Time Tracer - Admin"
  contents = adminPanel
  listenTo(timesRdo, empsRdo, usersRdo, projsRdo, rolesRdo, logsRdo, applyBtn, undoBtn, closeBtn, chPwdBtn)
  reactions += {
    case ButtonClicked(`timesRdo`) => readTable("SELECT * FROM TimeTracer.Times ORDER BY PunchedTime DESC", "Times")
    case ButtonClicked(`empsRdo`) => readTable("SELECT * FROM TimeTracer.Employees", "Employees")
    case ButtonClicked(`usersRdo`) => readTable("SELECT UserName, RoleID, EmployeeID FROM TimeTracer.Users", "Users")
    case ButtonClicked(`projsRdo`) => readTable("SELECT * FROM TimeTracer.Projects", "Projects")
    case ButtonClicked(`rolesRdo`) => readTable("SELECT * FROM TimeTracer.Roles", "Roles")
    case ButtonClicked(`logsRdo`) => readTable("SELECT * FROM TimeTracer.Log", "Logs")
    case ButtonClicked(`applyBtn`) => applyChange()
    case ButtonClicked(`undoBtn`) => resetChanges()
    case ButtonClicked(`closeBtn`) => System.exit(0)
    case ButtonClicked(`chPwdBtn`) => hide()
      ChangePasswordUI("Admin", 1, 0, false)
  }
  timesRdo.doClick()
  pack()
  centerOnScreen()
}
