⚡ GraphQL + QueryDSL Dynamic Query Engine

A demonstration of field-level optimized querying using GraphQL and type-safe SQL generation via QueryDSL. CSV is used only as a demo dataset.

⸻

🚀 Overview

This project showcases how GraphQL and QueryDSL can be combined to build a dynamic, efficient query engine capable of:
	•	Understanding natural-language questions
	•	Translating them into structured query instructions
	•	Generating optimized SQL using QueryDSL
	•	Fetching only the required fields
	•	Returning results through a GraphQL API

CSV upload is simply a way to provide sample data for demonstration; the core of the project is the GraphQL + QueryDSL pipeline.

⸻

🎯 Problem Statement

Typical GraphQL tutorials fetch entire tables and filter data in Java, causing:
	•	Over-fetching
	•	Poor performance
	•	Unnecessary memory usage
	•	Inability to support dynamic user queries

This project solves that by generating SQL that matches the exact user request, not retrieving unnecessary columns or rows.

⸻

🧩 Why GraphQL Instead of REST?
	•	REST returns fixed fields and fixed structures.
	•	GraphQL allows the user to request exact fields they need.
	•	Perfect for natural-language–driven systems where each query is different.

GraphQL → prevents over-fetching.
QueryDSL → prevents over-querying.

⸻

🧮 Why QueryDSL?

QueryDSL allows the backend to:
	•	Build SQL queries dynamically
	•	Add filters, sorting, limits at runtime
	•	Ensure type-safety (compile-time error checking)
	•	Generate efficient SQL instead of using SELECT *
	•	Convert natural-language filters into real database queries

Java does no filtering or sorting — everything is optimized at the SQL level.

⸻

🗄️ Why H2 Database?
	•	In-memory
	•	Fast startup
	•	No configuration
	•	Perfect for demonstration environments

The engine works exactly the same with MySQL, PostgreSQL, or any production SQL database.

⸻

🌟 Core Idea (Gist)

This project demonstrates how GraphQL and QueryDSL can work together to avoid both over-fetching (GraphQL) and over-querying (QueryDSL).
Instead of loading all data and filtering in Java, the system generates optimized SQL based on the user’s natural-language question, retrieves only the necessary rows and fields, and returns them through GraphQL.

CSV is simply a sample dataset to showcase the dynamic query engine.

⸻

🧱 Architecture

User Question (English)
          ↓
GraphQL Endpoint (ask)
          ↓
Natural Language Parser
          ↓
QueryDSL SQL Generator
          ↓
H2 Database
          ↓
GraphQL Response (requested fields only)


⸻

🧪 Example Queries

Simple Query

show all rows where city is Paris

Complex Query

show top 3 rows where department is Engineering 
and experience greater than 3 
order by salary desc


⸻

📁 Technologies Used
	•	Spring Boot
	•	Spring GraphQL
	•	QueryDSL (JPA)
	•	Java 17
	•	H2
	•	Maven

⸻

▶️ How to Run

Start the application

mvn spring-boot:run

Upload a CSV

curl -X POST http://localhost:8081/api/upload-csv \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/sample.csv"

Ask a GraphQL Question

curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ ask(question:\"show all rows where city is Paris\") { id cells { columnName value } } }"}'


⸻

🏁 Conclusion

This project demonstrates a modern, scalable approach to query optimization:
	•	GraphQL → flexible, field-level querying
	•	QueryDSL → dynamic, type-safe SQL generation
	•	H2 → a fast demo environment

Together, they form a highly optimized natural-language query system.

⸻


https://github.com/user-attachments/assets/38f08bb3-a3fe-40a5-a8ff-0aa67da6be78


