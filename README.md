# CraftyLang

CraftyLang is a small programming language embedded within Minecraft! It requires Fabric Loader 0.15+ (for Minecraft 1.20.4).

Heavily inspired by [Crafting Interpreters](http://craftinginterpreters.com/).

## Features
- Minimal error handling/messages
- Basic types: string, number, boolean, null
- Basic operators: +, -, /, *
- Variable declaration
- For/While loops
- Say statement (similar to printing)
- Commenting with "#"
- Control flow (if/else)
- Unary operations (!/-)
- Logical operators

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
