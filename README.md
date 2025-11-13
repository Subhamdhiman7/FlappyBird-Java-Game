Flappy Bird – Java Game (Swing & Graphics2D)
A fully interactive desktop version of the classic Flappy Bird game built using Java Swing, Graphics2D, and Object-Oriented Programming.

🎮 Game Overview
This project recreates Flappy Bird with:

Smooth bird movement using gravity physics

Procedurally generated pipes

Collectible coins

Clouds, particles, animations, and polished UI

Welcome screen + Play button

Game Over screen with high score tracking

The game runs as a standalone JAR file.

🚀 Features
⭐ Gameplay
Gravity-based bird movement

Spacebar to jump

Procedural pipe generation

Coin spawning & coin counter

Increasing score on successful passes

Collision detection for pipes and ground

Particle effects for collisions and interactions

⭐ UI & Graphics
Custom welcome screen with Play button

Sky gradient background

Animated clouds

Detailed pipe & coin rendering

Bird rotation, wing animation

Game Over overlay

Smooth Graphics2D rendering

⭐ System Features
Built using Java Swing GUI

Game loop implemented using Swing Timer

Event handling using KeyListener & MouseListener

Object-oriented class structure (Bird, Pipe, Coin, Cloud, Particle)

🧩 Tech Stack
Component	Technology
Language	Java
UI Framework	Swing
Graphics	Graphics2D API
Data Structures	CopyOnWriteArrayList, Iterators
Build	JDK (javac, jar tool)
Packaging	Runnable JAR
Version Control	Git + GitHub
🧠 Data Structures Used
✔ CopyOnWriteArrayList
Used for:

Pipes

Clouds

Coins

Particles

Why?
Because it allows safe item removal while iterating during the game loop.

✔ Iterators
Safely updates and removes off-screen objects.

✔ OOP Classes
Bird – movement, rotation, jump physics

Pipe – gap, collision detection

Coin – animation & drawing

Cloud – background animation

Particle – effects

GamePanel – core game loop

FlappyBirdGame – main entry point, JFrame setup

🕹️ How to Play
SPACE → Jump

Avoid pipes

Collect coins

Pass through pipes to gain score

Survive as long as possible!

Press SPACE to restart after Game Over

Click PLAY on welcome screen to start

🏗️ How to Run the Game
Option 1 — Run the JAR file
Double-click the file:

FlappyBirdGame.jar
Or run from terminal:

java -jar FlappyBirdGame.jar
Option 2 — Run from source
Open CMD inside the folder and run:

javac FlappyBirdGame.java
java FlappyBirdGame
📦 Project Structure
FlappyBirdGame.java   (Main + all classes)
MANIFEST.MF            (Main-Class reference)
FlappyBirdGame.jar     (Executable game)
All classes are written inside one file for simplicity.

🔥 Future Enhancements
Sound effects (jump, score, collision)

Difficulty scaling (pipe speed increases)

Power-ups

Night mode / theme changes

Multiplayer mode

👨‍💻 Developers
Ayush

Shubham

Vaishali

⭐ Why This Project Is Valuable
Demonstrates mastery of Java Swing

Shows understanding of game loops

Uses OOP, DSA, and real-time rendering

Perfect for portfolio, viva, or academic evaluation
