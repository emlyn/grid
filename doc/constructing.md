# Constructing a grid

## A string

Each cell of the grid will contain a character from the string.
The string will be converted to 2 dimensions by splitting on newline characters.
If the lines are not of equal length, the grid will be as wide as the longest line,
with missing characters filled with `nil`.

```clojure
(g/grid "hello\nworld")
```
```text
| h | e | l | l | o |
| w | o | r | l | d |
```

If dimensions are specified, you can leave out the newline characters.
In this case the length of the string must exactly match the total number of cells:

```clojure
(g/grid 3 3 "abcdefghi")
```
```text
| a | b | c |
| d | e | f |
| g | h | i |
```

## A sequence of strings

You can instead supply a sequence of strings, where each string will represent one row in the grid,
even if it contains newline characters.
Note that newline will be printed normally, messing up the output,
and the second row is padded with a `nil` which prints as the empty string.

```clojure
(g/grid ["grid\nwith" "newlines"])
```
```text
| g | r | i | d |
 | w | i | t | h |
| n | e | w | l |  i | n | e | s |   |
```

## A flat sequence of values

You can specify the dimensions of the grid, and supply a sequence of any values,
of length exactly equal to the total number of cells:

```clojure
(g/grid 4 3 [1 2 3 4 2 4 6 8 3 6 9 12])
```
```text
| 1 | 2 | 3 |  4 |
| 2 | 4 | 6 |  8 |
| 3 | 6 | 9 | 12 |
```

## A map of [x y] coordinate to value

You can supply a map, where the keys are vectors of 2 non-negative integers representing x-y coordinates.
The specified coordinates will be filled with the given values, and unspecified cells will contain `nil`.

```clojure
(g/grid {[1 0] 1, [0 1] 4, [2 1] 2, [1 2] 3})
```
```text
| `nil` |   1   | `nil` |
|   4   | `nil` |   2   |
| `nil` |   3   | `nil` |
```

## Associative (map or vector) of associative

The outer one maps y coordinate

## Another Grid

You can initialise grid cells from another grid.
Note however that both grids will have the same origin as there is no way to specify the starting point,
only the size of the new grid.

```clojure
(def board (g/grid [[1 2 3] [4 5 6]]))
(g/grid 2 2 board)
```
```text
| 1 | 2 |
| 4 | 5 |
```

For a more flexible way to create a subgrid from another grid, you can use slice indexing (see ...).

## Constant value

If you want all cells to have the same value, you can use the `everywhere` helper:

```clojure
(g/grid 2 3 (everywhere 42))
```
```text
| 42 | 42 |
| 42 | 42 |
| 42 | 42 |
```

## A function

```clojure
(g/grid 4 4 *)
```
```text
| 0 | 0 | 0 | 0 |
| 0 | 1 | 2 | 3 |
| 0 | 2 | 4 | 6 |
| 0 | 3 | 6 | 9 |
```

## Reader macro

The grid constructor function is also availeble as a reader macro (`#emlyn/grid`).
This can be useful if you already have a large data structure representing the contents of the grid;
then you only need to add `#emlyn/grid` in front of it, instead of having to wrap parentheses around it:

```clojure
#emlyn/grid
[[1 2 3 4 5]
 [2 4 6 8 10]
 [3 6 9 12 15]
 [4 8 12 16 20]
 [5 10 15 20 25]]
 ```
```text
| 1 |  2 |  3 |  4 |  5 |
| 2 |  4 |  6 |  8 | 10 |
| 3 |  6 |  9 | 12 | 15 |
| 4 |  8 | 12 | 16 | 20 |
| 5 | 10 | 15 | 20 | 25 |
```

Note that it is not possible to specify the size in this case,
since the macro only operates on the single form following it,
so you have to use a structure for which the size can be inferred.
