package org.example.employee;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.regex.Pattern;

class Employee {
    public static Document createDocument(String name, String email, int age, String department, List<String> skills, String joiningDate, double salary) {
        return new Document("name", name)
                .append("email", email)
                .append("age", age)
                .append("department", department)
                .append("skills", skills)
                .append("joiningDate", joiningDate)
                .append("salary", salary);
    }
}

class EmployeeManagementSystem {
    private final MongoCollection<Document> collection;

    public EmployeeManagementSystem() {
        this.collection = MongoConnection.getDatabase().getCollection("employees");
    }

    public void addEmployee(Document emp) {
        if (collection.find(Filters.eq("email", emp.getString("email"))).first() != null) {
            System.out.println("❌ Employee with this email already exists!");
            return;
        }
        collection.insertOne(emp);
        System.out.println("✅ Employee added.");
    }

    public void updateEmployee(String email, Bson updates) {
        System.out.println("🔍 Before update:");
        collection.find(Filters.eq("email", email)).forEach(System.out::println);

        UpdateResult result = collection.updateOne(Filters.eq("email", email), updates);

        if (result.getMatchedCount() == 0) {
            System.out.println("❌ No employee found with that email.");
        } else if (result.getModifiedCount() == 0) {
            System.out.println("⚠️ Employee found but no changes were made.");
        } else {
            System.out.println("✅ Employee updated successfully.");
        }

        System.out.println("📄 After update:");
        collection.find(Filters.eq("email", email)).forEach(System.out::println);
    }

    public void deleteEmployeeByEmail(String email) {
        collection.deleteOne(Filters.eq("email", email));
        System.out.println("🗑️ Deleted by email.");
    }

    public void deleteEmployeeById(String id) {
        collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
        System.out.println("🗑️ Deleted by ID.");
    }

    public void searchByName(String keyword) {
        Pattern regex = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
        collection.find(Filters.regex("name", regex)).forEach(System.out::println);
    }

    public void searchByDepartment(String dept) {
        collection.find(Filters.eq("department", dept)).forEach(System.out::println);
    }

    public void searchBySkill(String skill) {
        collection.find(Filters.in("skills", skill)).forEach(System.out::println);
    }

    public void searchByJoiningDateRange(String from, String to) {
        collection.find(Filters.and(
                Filters.gte("joiningDate", from),
                Filters.lte("joiningDate", to)
        )).forEach(System.out::println);
    }

    public void listEmployees(int page, int size, String sortBy) {
        collection.find()
                .sort(Sorts.ascending(sortBy))
                .skip((page - 1) * size)
                .limit(size)
                .forEach(System.out::println);
    }

    public void departmentStats() {
        collection.aggregate(List.of(
                Aggregates.group("$department", Accumulators.sum("count", 1))
        )).forEach(System.out::println);
    }
}

public class EmployeeManagementApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        EmployeeManagementSystem ems = new EmployeeManagementSystem();
        int choice;

        do {
            System.out.println("1. Add Employee");
            System.out.println("2. Update Employee");
            System.out.println("3. Delete Employee");
            System.out.println("4. Search Employee");
            System.out.println("5. List Employees with Pagination");
            System.out.println("6. Department Stats");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt(); sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Name: "); String name = sc.nextLine();
                    System.out.print("Email: "); String email = sc.nextLine();
                    System.out.print("Age: "); int age = sc.nextInt(); sc.nextLine();
                    System.out.print("Department: "); String dept = sc.nextLine();
                    System.out.print("Skills (comma-separated): ");
                    List<String> skills = Arrays.asList(sc.nextLine().split(","));
                    System.out.print("Joining Date (YYYY-MM-DD): "); String join = sc.nextLine();
                    System.out.print("Salary: "); double salary = sc.nextDouble(); sc.nextLine();
                    ems.addEmployee(Employee.createDocument(name, email, age, dept, skills, join, salary));
                    break;

                case 2:
                    System.out.print("Email to update: "); String updateEmail = sc.nextLine();
                    System.out.print("New Department: "); String newDept = sc.nextLine();
                    System.out.print("New Skill to Add: "); String newSkill = sc.nextLine();
                    ems.updateEmployee(updateEmail, Updates.combine(
                            Updates.set("department", newDept),
                            Updates.addToSet("skills", newSkill)));
                    break;

                case 3:
                    System.out.println("Delete by: 1. Email  2. ID");
                    int d = sc.nextInt(); sc.nextLine();
                    if (d == 1) {
                        System.out.print("Email: "); ems.deleteEmployeeByEmail(sc.nextLine());
                    } else {
                        System.out.print("ID: "); ems.deleteEmployeeById(sc.nextLine());
                    }
                    break;

                case 4:
                    System.out.println("Search by: 1. Name  2. Department  3. Skill  4. Joining Date Range");
                    int s = sc.nextInt(); sc.nextLine();
                    switch (s) {
                        case 1: System.out.print("Name Keyword: "); ems.searchByName(sc.nextLine()); break;
                        case 2: System.out.print("Department: "); ems.searchByDepartment(sc.nextLine()); break;
                        case 3: System.out.print("Skill: "); ems.searchBySkill(sc.nextLine()); break;
                        case 4:
                            System.out.print("From (YYYY-MM-DD): "); String from = sc.nextLine();
                            System.out.print("To (YYYY-MM-DD): "); String to = sc.nextLine();
                            ems.searchByJoiningDateRange(from, to);
                            break;
                    }
                    break;

                case 5:
                    System.out.print("Page Number: "); int p = sc.nextInt(); sc.nextLine();
                    System.out.print("Sort by (name/joiningDate): "); String sortBy = sc.nextLine();
                    ems.listEmployees(p, 5, sortBy);
                    break;

                case 6:
                    System.out.println("📊 Department Statistics:");
                    ems.departmentStats();
                    break;

                case 7:
                    System.out.println("👋 Exiting the system. Goodbye!");
                    break;

                default:
                    System.out.println("❗ Invalid choice. Try again.");
            }

        } while (choice != 7);
        sc.close();
    }
}
