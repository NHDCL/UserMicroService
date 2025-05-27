# NHDCL Facility Management System

Welcome to the official GitHub organization for the National Housing Development Corporation Limited (NHDCL) Facility Management System.

## Purpose

The NHDCL Facility Management System (FMS) is a comprehensive digital platform designed to streamline the management of assets and maintenance operations for the National Housing Development Corporation Limited (NHDCL), which oversees over 300 buildings across five Gyalsung Academies in Bhutan. The system addresses inefficiencies in asset tracking, maintenance scheduling, and data centralization, aiming to enhance operational efficiency, reduce costs, and improve service delivery for facility management.

## Architecture

This project follows a microservices architecture, with separate repositories for each core service:

- **Frontend:** Built with React.js, offering responsive UI/UX for users, managers, technicians, and admins.
- **Backend:**  Developed using Spring Boot (Java), handling authentication, business logic, and data services.
- **Database:** MongoDB, storing structured asset and user data.
- **API Gateway:** Secure routing of requests with role-based access.
- **(Add other services as needed, e.g., Mobile App, Notification Service, etc.)**

Each service is developed, deployed, and maintained independently for scalability and flexibility.

## Functionalities:
Key features of the NHDCL FMS include:

- **User Role Management:** Super Admin, Admin, Manager, Supervisor, Technician, General User

- **Asset Management:** Track, add, update, bulk upload, and tag assets with QR codes

- **Maintenance Management:** Submit requests, assign tasks, update status (Pending, In Progress, Completed)

- **Scheduling:** Create and reschedule tasks with centralized calendar & alerts

- **Mobile Technician Interface:** On-site updates and status tracking

- **Reports & Exports:** Generate and download maintenance and asset reports (CSV, PDF)

- **Security:** JWT-based authentication, encrypted data, and secure user access


## Contact & Contribution

For inquiries or contributions, please contact 12210100.gcit@rub.edu.bt or open an issue in the relevant repository.

---

*This organization is maintained by the NHDCL Facility Management Team.*
