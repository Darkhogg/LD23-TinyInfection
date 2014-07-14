Tiny Infection
==============

Tiny Infection is a game originally created for [Ludum Dare 23][ld23]. The game
was created from scratch and entirely by me in less than 48 hours.

  [ld23]: http://www.ludumdare.com/compo/ludum-dare-23/

Tiny infection is a shameless clone/mixup of [Phage Wars], [Galcon] and
[Eufloria] with a few personal touches.

  [Phage Wars]: http://armorgames.com/play/2675/phage-wars
  [Galcon]:     http://www.galcon.com/
  [Eufloria]:   http://www.eufloria-game.com/

The objective of the game is to infect cells. You start with a cell already
infected, and must move your viruses to nearby cells so they produce more
viruses for you. You must erradicate your adversaries, two other virus types,
also playable. The AI is very simplistic, but can overrun you sometimes because
of its nonpredictability.


### Controls

| Key                | Action |
|-------------------:|:-------|
| *Left Click*       | Selects a cell.
| *Right Click*      | Moves viruses to another cell.
| `1`, `2`, `3`      | Selects a virus type (triangle, square, circle,
                       respectively) for the movement (keep the key pressed).
                       Pressing multiple keys selects more than one.
| `W`, `A`, `S`, `D` | Move the camera around.


Building/Running the Game
-------------------------

In the [releases](../../releases) page you can find pre-compiled versions of the game
in JAR format. In most systems you should be able to double-click the file to
run it if you have Java installed. If you have problems, the Internet is full of
advice on how to execute a runnable JAR.

If you want to build the game from source, the easiest way is to install Ant and
run the `ant` command inside of the game directory. Running `ant run` will
have the same effect, in addition to immediately run the game.