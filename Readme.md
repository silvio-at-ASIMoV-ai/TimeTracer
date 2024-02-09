# Time Tracer

A simple app to manage employee's time on projects

## Getting Started

You can download the project and either make changes or just build it as is.

To build a jar you need sbt installed, run the command  `sbt assembly` and it will create
a jar in the *target/scala-3.3.1* folder

### Prerequisites

- JRE at least 1.8
- Scala
- sbt

### Installing and configuring

1. Just run the jar.
2. The first time you run it, it will ask for the connection parameters to connect to a mysql database.
3. After a successful creation of the database it will show a login window.

![Login.png](ReadmeResources%2FLogin.png)

### Using the App

1. The first login can be made using the user "Admin" and any password.
2. It will then prompt you to create a new password.
3. Once in the Admin console you just need to add:
    - projects in the projects table
    - employees in the employees table
    - users in the users table (don't forget to insert the employee id for each user)
4. To change table click on the corresponding radio button
---
![AdminConsole.png](ReadmeResources%2FAdminConsole.png)
![date.png](ReadmeResources%2Fdate.png) ![time.png](ReadmeResources%2Ftime.png)
5. To insert rows click inside the table and press the down arrow on the last row or anywhere on the table if there are no rows
6. To modify data in the table just click on the field and start typing
   - date and time fields will show date and time pickers
7. To delete a row press *ctrl-del*
8. The *Logs* table is read only
9. To change the Admin password just click on the *Ch. Pwd* button
10. To reset a user's password, open the *Users* table, select the user and click on the *Rst Pwd* button
11. The *Reports* button will show the Reports dialog

Any change will disable the radio buttons and show a "change pending" text, now you have the option to *apply* or *undo* the change with the corresponding button

---
![FirstLoginChangePassword.png](ReadmeResources%2FFirstLoginChangePassword.png)
![PunchInForm.png](ReadmeResources%2FPunchInForm.png)

12. Once  those tables are ready, the users can do their first login with their username and any password, then they will be prompted to change their password.
13. Any non Admin user will then access their punch-in form.
14. In the punch-in form they can choose the project to work on, and if they are punching IN or OUT.
15. If there is at least 1 record for that employee in the *Times* table, the fields will be pre-filled and the last punch-in time will show on the bottom of the form.
16. The *OK* button will just record the data and exit
17. The *Accept* button will record the data but will not exit
18. The *Remind* button will show a small dialog showing the punch-in time and the project, with the option to close or go back to the punch-in form
19. The *Ch. Pwd* will let the user change their password

![Reminder.png](ReadmeResources%2FReminder.png)
![ChangePassword.png](ReadmeResources%2FChangePassword.png)
---
20. All *OK* buttons can be clicked with the *Enter* key.
21. All *Cancel* buttons can be clicked with the *Esc* key.
22. You can navigate all fields with the *Tab* key in a natural order.
---
![reports.png](ReadmeResources%2Freports.png)

The Reports dialog lets you generate reports about the times employees work on projects
23. The Report Type dropdown has 3 report types
    - Hours worked on project.
    - Hours worked by employee.
    - Hours worked on project by employee.
24. You can choose a period of time by choosing appropriate from and to dates
25. You can choose a project and/or an employee
26. The button *Generate* opens the default browser with the chosen report in HTML.
27. *Close* closes the app, *Back* goes back to the Admin console

Example reports:
![report1.png](ReadmeResources%2Freport1.png)
![report2.png](ReadmeResources%2Freport2.png)

### Built With

- Scala 3.3.1 https://scala-lang.org/
- Scala Swing https://index.scala-lang.org/scala/scala-swing
- Flatlaf https://www.formdev.com/flatlaf/
- LGoodDatePicker https://github.com/LGoodDatePicker/LGoodDatePicker
- MySQL https://www.mysql.com/it/

### Authors

- **Silvestre Pitti**
- other authors welcome

### License

This project is licensed under the "You can do whatever you like with it" licence

