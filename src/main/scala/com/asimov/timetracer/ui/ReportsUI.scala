package com.asimov.timetracer.ui

import com.asimov.timetracer.{MyButton, MyLabel, MySQL}
import com.github.lgooddatepicker.components.{DatePicker, DatePickerSettings}

import java.awt.Desktop
import java.io.PrintWriter
import java.net.URI
import java.text.SimpleDateFormat
import java.time.LocalDate
import javax.swing.Box
import scala.swing.Orientation.Vertical
import scala.swing.event.ButtonClicked
import scala.swing.{BoxPanel, Button, ComboBox, Dialog, FlowPanel}

private case class ReportData(name: String, project: String, time: String, in: Boolean)

object ReportsUI extends Dialog {
  private val db = MySQL
  private val employees = getEmployees
  private val projects = getProjects
  private val q1 = """
             |SELECT e.`Name`, e.`Surname`, p.`ProjectName`, t.`PunchedTime`, t.`In`  FROM `TimeTracer`.`Times` t
             |INNER JOIN `TimeTracer`.`Projects` p ON p.`IDProject` = t.`ProjectID`
             |INNER JOIN `TimeTracer`.`Employees` e ON e.`IDEmployee` = t.`EmployeeID`
             |WHERE (DATE(`PunchedTime`) BETWEEN ? AND ?)
             |""".stripMargin
  private val q2 = " AND `ProjectID` = ?"
  private val q3 = " AND `EmployeeID` = ?"
  private val q4 = " ORDER BY `PunchedTime`"
  private val reportSQLMap: Map[Int, (String, String)] = Map(
    0 -> ("Hours worked on project", q1 + q2 + q4),
    1 -> ("Hours worked by employee", q1 + q3 + q4),
    2 -> ("Hours worked on project by employee", q1 + q2 + q3 + q4))

  override def closeOperation(): Unit = System.exit(0)

  def apply(): Unit = show()

  private def show(): Unit = visible = true

  private def hide(): Unit = visible = false

  private def getData(report: Int, from: String, to: String, projID: Int, empID: Int): List[ReportData] = {
    val query = reportSQLMap(report)._2
    db.connection match {
      case Some(conn) =>
        val statement = conn.prepareStatement(query)
        statement.setString(1, from)
        statement.setString(2, to)
        report match {
          case 0 => statement.setInt(3, projID)
          case 1 => statement.setInt(3, empID)
          case 2 => statement.setInt(3, projID)
            statement.setInt(4, empID)
          case _ =>
        }
        val rs = statement.executeQuery()
        Iterator.from(0).takeWhile(_ => rs.next())
          .map(_ => ReportData(s"${rs.getString(1)} ${rs.getString(2)}",
            rs.getString(3),
            rs.getString(4).dropRight(3),
            rs.getInt(5) == 1))
          .toList
      case _ => Nil
    }
  }

  private def getSummary(reportType: Int, reportData: List[ReportData]): String = {
    def convert2hmStr(mins: Long): String = {
      val hours = if(mins / 60 > 0) (mins / 60).toString + " hours " else ""
      val minutes = if(mins % 60 > 0) (mins % 60).toString + " minutes" else ""
      hours + minutes
    }

    val formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm")
    val grouped = reportType match {
      case 0 => reportData.groupBy(_.name)
      case 1 => reportData.groupBy(_.project)
      case 2 => reportData.groupBy(_.name)
      case _ => Nil
    }
    val sums = for (elem <- grouped) yield {
      val times = if(elem._2.last.in) elem._2.dropRight(1) else elem._2   //remove last time if it's IN
      val diffs = for(time <- times.grouped(2)) yield {
        val from = formatter.parse(time.head.time).getTime
        val to = formatter.parse(time(1).time).getTime
        (to - from) / 60000
      }
      (elem._1, diffs.sum)
    }
    val summary = sums.map(r => s"<td>${r._1}</td><td>${convert2hmStr(r._2)}</td>")
      .mkString("<tr>", "</tr><tr>", "</tr>")
    val total = s"<td><font color='red'><b>Total</b></font></td>" +
      s"<td><font color='red'><b>${convert2hmStr(sums.map(_._2).sum)}</b></font></td>"
    summary + total
  }

  private def genReport(from: String, to: String, projID: Int, empID: Int): Unit = {
    val report = reportTypes.selection.index
    val reportData = getData(report, from, to, projID, empID)
    val rows = reportData.map(rd =>
      s"""<td>${rd.name}</td>
         |<td>${rd.project}</td>
         |<td>${rd.time}</td>
         |<td>${if(rd.in) "In" else "Out"}
         |""".stripMargin)
      .mkString("<tr>", "</tr><tr>", "</tr>")
    val reportFileName = s"${System.getProperty("user.home")
      .replaceAll("\\\\", "/")}/.TimeTracer/CurrentReport.html"
      .replaceAll(" ", "%20")
    val h1 = report match {
      case 0 =>
        s"""<h1>${reportTypes.selection.item}
           |"${project.selection.item}"
           | from $from to $to</h1>""".stripMargin
      case 1 =>
        s"""<h1>${reportTypes.selection.item}
           |"${employee.selection.item}"
           | from $from to $to</h1>""".stripMargin
      case 2 =>
        s"""<h1>Hours worked on project
           |"${project.selection.item}" by employee
           |"${employee.selection.item}"
           | from $from to $to</h1>""".stripMargin
      case _ => ""
    }
    val style =
      """
        |<style>
        |* {
        |  box-sizing: border-box;
        |}
        |h1 {
        | text-align: center;
        | font-size: 300%;
        | margin: 40px;
        |}
        |
        |.row {
        |  margin-left:-5px;
        |  margin-right:-5px;
        |}
        |
        |.column {
        |  float: left;
        |  width: 50%;
        |  padding: 5px;
        |}
        |
        |/* Clearfix (clear floats) */
        |.row::after {
        |  content: "";
        |  clear: both;
        |  display: table;
        |}
        |
        |table {
        |  margin: auto;
        |  border-collapse: collapse;
        |  border-spacing: 0;
        |  width: 80%;
        |  border: 1px solid #ddd;
        |}
        |
        |th, td {
        |  text-align: left;
        |  padding: 16px;
        |}
        |
        |tr:nth-child(even) {
        |  background-color: #f2f2f2;
        |}
        |</style>
        |""".stripMargin
    val pre =
      s"""
         |<html>
         |<head>
         |$style
         |</head>
         |<body>
         |$h1
         |""".stripMargin
    val table =
      s"""
        |<table>
        |<tr>
        |    <th>Employee</th>
        |    <th>Project</th>
        |    <th>Time</th>
        |    <th>In/Out</th>
        |</tr>
        |$rows
        |</table>
        |""".stripMargin
    val riepilogo = getSummary(report, reportData)
    val post = "</body></html>"
    val div1 =  "<div class='row'><div class='column'>"
    val div2 =  "</div><div class='column'>"
    val div3 =  "</table></div></div>"
    val riepHead = if(report == 1) s"<table><tr><th>Project</th><th>Total Time</th></tr>"
    else s"<table><tr><th>Employee</th><th>Total Time</th></tr>"
    val html = pre + div1 + table + div2 + riepHead + riepilogo + div3 + post
    val pw = new PrintWriter(reportFileName)
    pw.write(html)
    pw.close()
    if (Desktop.isDesktopSupported) {
      Desktop.getDesktop.browse(new URI(s"file:///$reportFileName"))
    } else {
      // warn user that we can't open the browser
    }
  }

  private def getEmployees: Map[String, Int] = {
    db.connection match {
      case Some(conn) =>
        val query = "SELECT `IDEmployee`, `Name`, `Surname` FROM `TimeTracer`.`Employees` ORDER BY `Surname`"
        val rs = conn.createStatement().executeQuery(query)
        Iterator.from(0).takeWhile(_ => rs.next())
          .map(_ => (s"${rs.getString(2)} ${rs.getString(3)}", rs.getInt(1)))
          .toMap
      case _ => Map()
    }
  }

  private def getProjects: Map[String, Int] = {
    db.connection match {
      case Some(conn) =>
        val query = "SELECT `IDProject`, `ProjectName` FROM `TimeTracer`.`Projects`"
        val rs = conn.createStatement().executeQuery(query)
        Iterator.from(0).takeWhile(_ => rs.next())
          .map(_ => (rs.getString(2), rs.getInt(1)))
          .toMap
      case _ => Map()
    }
  }

  private val reportTypes: ComboBox[String] = new ComboBox[String](reportSQLMap.values.map(_._1).toList)
  private val dateSettings1 = new DatePickerSettings
  dateSettings1.setFormatForDatesCommonEra("yyyy/MM/dd")
  private val dateSettings2 = new DatePickerSettings
  dateSettings2.setFormatForDatesCommonEra("yyyy/MM/dd")
  private val from = new DatePicker(dateSettings1)
  private val project: ComboBox[String] = new ComboBox[String](projects.keys.toList)
  private val to = new DatePicker(dateSettings2)
  private val employee: ComboBox[String] = new ComboBox[String](employees.keys.toList)
  private val genBtn: Button = new MyButton("Generate")
  private val closeBtn: Button = new MyButton("Close")
  private val backBtn: Button = new MyButton("Back")

  private val leftPanel = new MyMigPanel {
    colConstraints = "[200px][150px][150px][10px]"
    rowConstraints = "[20px][20px][20px][20px][15px]"
    add(MyLabel(" Report Type"), "cell 0 0, gapx 10px, aligny center, alignx left")
    add(reportTypes, "cell 0 1, gapx 10px, growx, aligny center, alignx left")
    add(MyLabel(" From"), "cell 1 0, gapx 10px, aligny center, alignx left")
    peer.add(from, "cell 1 1, gapx 10px, growx, aligny center, alignx left")
    add(MyLabel(" Project"), "cell 1 2, gapx 10px, aligny center, alignx left")
    add(project, "cell 1 3, gapx 10px, growx, aligny center, alignx left")
    add(MyLabel(" To"), "cell 2 0, gapx 10px, aligny center, alignx left")
    peer.add(to, "cell 2 1, gapx 10px, growx, aligny center, alignx left")
    add(MyLabel(" Employee"), "cell 2 2, gapx 10px, aligny center, alignx left")
    add(employee, "cell 2 3, gapx 10px, growx, aligny center, alignx left")
  }

  private val rightPanel = new BoxPanel(Vertical) {
    contents += genBtn
    contents += closeBtn
    contents += backBtn
  }

  private val reportPanel = new FlowPanel {
    contents += leftPanel
    contents += rightPanel
    peer.add(Box.createHorizontalStrut(15))
  }

  title = "Time Tracer - Reports"
  contents = reportPanel
  listenTo(closeBtn, backBtn, genBtn)
  reactions += {
    case ButtonClicked(`genBtn`) => genReport(from.getText, to.getText,
      projects(project.selection.item),
      employees(employee.selection.item))
    case ButtonClicked(`closeBtn`) => System.exit(0)
    case ButtonClicked(`backBtn`) => hide()
      AdminUI()
  }
  pack()
  centerOnScreen()
  from.setDate(LocalDate.now().withDayOfMonth(1))
  try {
    to.setDate(LocalDate.now().withDayOfMonth(31))
  } catch {
    case _ => try {
      to.setDate(LocalDate.now().withDayOfMonth(30))
    } catch {
      case _ => try {
        to.setDate(LocalDate.now().withDayOfMonth(29))
      } catch
        case _ => 
          to.setDate(LocalDate.now().withDayOfMonth(28))
    }
  }
}
