
# E-shop

Developed a secure user and inventory management system for an online shop using **React** and **Spring Boot**. Implemented API testing with **Postman**, secure coding practices including password salting, CAPTCHA, exception handling, session management, SQL injection prevention, and input validation on both frontend and backend.

---

## 🛠 Technologies Used

- **Frontend**: React.js  
- **Backend**: Spring Boot (Maven)  
- **Database**: MySQL  
- **Email Service**: Gmail SMTP  
- **Tools**: VS Code, IntelliJ IDEA, MySQL Workbench, Postman

---

## 🚀 Features

- User registration and login with password encryption and CAPTCHA
- User CRUD operations  
- Inventory CRUD operations  
- Email notifications using Gmail SMTP  
- Secure API communication with validation and exception handling  
- Separate admin and user roles  
- Responsive UI

---

## 🔧 How to Run the Project

## 1️⃣ Start MySQL Database  
Make sure MySQL is running and create the database:

```sql
CREATE DATABASE inventory_management;
```

---
## 2️⃣ Run Backend

cd backend          <br>
mvn clean install   <br>
mvn spring-boot:run <br>

---
## 3️⃣ Run Frontend

cd frontend                                             <br>
npm install    # Run this once to install dependencies  <br>
npm start      # Start React development server          <br>

---
###✅ Your project should now be fully running:

-Frontend: http://localhost:3000 <br>
-Backend: http://localhost:8080  <br>



