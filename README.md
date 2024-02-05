# CraftyLang

CraftyLang is a small programming language embedded within Minecraft! It requires Fabric Loader 0.15+ (for Minecraft 1.20.4).

Heavily inspired by [Crafting Interpreters](http://craftinginterpreters.com/).

## Features
- Basic syntax highlighting
- Minimal error handling/messages
- Basic types: string, number, boolean, null
- Basic operators: +, -, /, *, %
- Variable declaration
- For/While loops
- Say statement (similar to printing)
- Commenting with "#"
- Control flow (if/else)
- Unary operations (!/-)
- Logical operators
- Functions
- Basic module system

## Usage
Every expression must end with a semicolon `;`.

### Comments
Lines starting with "#" are treated as comments and ignored.

### Variable Declaration
To declare a variable, use the `let` keyword followed by the variable name, then "=", and finally the initializer value.

For example:
- String: `let str = 'this is a string';`
  Strings also support concatenation with the "+" operator.
  ```
  let a = 'a';
  let b = 'b';
  let c = a + b; # that works
  ```

- Number: `let num = 10;`
- Boolean: `let val = T;` (T is true, F is false)

You can negate a number with a `-` in front of it
```
let a = 1;
let b = -a; # is -1
let c = -1; # this also works
```

You can negate booleans with a `!` in front of it
```
say !T; # is false
say !F; # is true
```

Variables can be reassigned like this
```
let a = 10;
say a;          # 10

a = 20;
say a;          # 20
```

### For Loops
For loops follow a C-like syntax. You can initialize the loop variable, specify the condition, and define the increment all in one line:
```
for (let var = <begin value>; var < <end value>; var = var + 1) {
    ...
}
```

### While Loops
While loops also follow a C-like style:
```
while (<condition>) {
    ...
}
```
*Note: There is a limit to the number of iterations a while loop can have, which can be configured in the settings (keybinding).*

### Control Flow
Control flow currently supports if/else statements:
```
if (<condition>) {
    ...
} else {
    ...
}
```

#### Logical operators:
- Equality:  `10 == 10;`
- Not Equal: `10 != 20;`
- Less than: `10 < 20;`
- Less than or equal: `10 <= 20;`
- Greater: `10 > 2;`
- Greater than or equal: `10 >= 2;`

### Say (Printing)
The `say` statement functions similarly to `print` in most languages but instead of writing to STDOUT, it writes to the chat. It's client-side, so no one else will see what you're writing.
You can use expressions with `say`, for example: `say 10 + 2;`.

### Scopes
Scopes are created by encapsulating some code inside `{}` for example
```
let a = 10;
{
    let a = 20;     # this won't conflict
}
```

However
```
{
    let a = 20;
}
say a;              # will produce an error because `a` is declared in another scope
```

### Functions
You can declare functions using the `fn` keyword, followed by the name of the function and the argument list.
```
fn function(a, b) {
  ret a + b;
}
```
The `ret` keyword specifies the return value of the function

### Modules
Ever wanted to import other pieces of code from another book and quill? Well you can do that now using modules.
To use a module you simply
```
!use <book name>
```
Assuming [book name] is a valid book and quill name that is in your inventory, the mod should copy the code from the book
and place it inside the code you are importing.

There is also a very basic (like very very basic) standard library with the only library being `math`
Similarly with code you have written you can just import it like this

```
!use math
```

The math module has some very basic functions such as `sqrt`, `pow`, `min` and `max`, `abs` (absolute value), 
`hypot` (the hypotenuse of a right angle triangle with sides a, b) and the beloved `PI` as a constant

All module functions/constants are declared globally so you can access them globally, from anywhere
```
!use math

sqrt(2);

# and also in here
fn function() {
  sqrt(3);
}
```

### Basic built-in global definitions
 - Current version: `VERSION` just returns the current version of CraftScript
 - Coordinates: `xc`, `yc` and `zc` are all globally defined variables that as you might have guessed contain the 
player's x, y, z coordinates.
 - Send Chat message: Unlike `say`, `glob(<'message'>)` sends a message as the player
 - Closing the book edit screen: `close()` does just that. It won't exit the program, just close the screen
 - Clearing the chat: This is what `clear()` does
 - Exiting: `exit(<code>)` will exit the program with the specified exit code (non-zero exit codes print an error)
 - String conversion: `str(<value>)` will hopefully convert the value into a CraftScript string that can be concatenated 
with the `+` operator
 - Attacking (buggy): `attack()` will... attack as the player

