# 🤖 Autonomous Robot Path Planning using A*

This project simulates autonomous robot navigation in a grid environment using the A* (A-star) pathfinding algorithm.

---

## 🚀 Features

* Interactive grid-based environment
* Obstacle placement and removal
* A* pathfinding visualization
* Robot movement animation
* Pause / Resume / Step execution
* Metrics display:

  * Nodes explored
  * Path cost
  * Efficiency

---

## 🧠 Algorithm Used

* A* (A-star) Algorithm
* Heuristic: Manhattan Distance

---

## 🧱 Project Structure

* `model/` → Algorithm logic (Node, Grid, AStarSolver)
* `ui/` → GUI components (Swing)
* `util/` → Theme and styling
* `app/` → Main entry point

---

## ▶️ How to Run

```bash
javac -d out src/app/Main.java src/ui/*.java src/model/*.java src/util/*.java
java -cp out app.Main
```

---

## 🎯 Use Case

This project demonstrates real-world robot navigation in:

* Autonomous robotics
* Path planning systems
* AI-based decision making

---

