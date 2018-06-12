# Kindred: Your own genealogy manager
Source code of the *Kindred* program.
Feature list:
1. Good-looking user interface: your genealogy visualised.
2. Loading and saving family trees.
3. Adding and removing persons and relations between them.
4. Editing profiles.
5. KISP (Kinship LISP new programming language) REPL mode.

## How to Run GUI
Download the compiled .jar archive: https://github.com/egryaznov/thesiscode/raw/master/out/artifacts/kindred_jar/kindred.jar
Run it from the terminal with the following command: `java -jar kindred.jar`

## How to Run KISP REPL
In order to run a KISP REPL, you need to provide the following as a command-line argument:
1. **Required** Path to a `.kindb` genealogy file, with `-g` argument key.
2. *Optional* Path to a pre-defined KISP script file, with `-a` argument key.

Example:
`java -jar kindred.jar -g my_genealogy.kindb -a axioms.lisp`

The REPL prompt will appear (`|-`) and you can start evaluating your KISP term, like that:

`|- (+ 2 2)`

`4`

`|- (map inc (list 1 2 3 4))`

`(list 2 3 4 5)`

Also, there are REPL commands that can be used to change and gather information about current execution enviroment ($time, $cache, etc).

## How to Use GUI
1. Create new genealogy by pressing Ctrl-N
2. Enter the title for your new genealogy
3. When the screen goes blue, start creating nodes and links between them:
  1. Double-click on any empty place to create new node
  2. Double-click on any node to edit its profile
  3. To create a link between two nodes, do a left click on the first, and the right click on the second. A context-menu should apper where you can select the type of your link.
