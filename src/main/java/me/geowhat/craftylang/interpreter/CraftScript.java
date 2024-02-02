package me.geowhat.craftylang.interpreter;

import me.geowhat.craftylang.client.util.Message;
import me.geowhat.craftylang.client.util.Scheduler;
import me.geowhat.craftylang.interpreter.ast.Parser;
import me.geowhat.craftylang.interpreter.ast.Statement;
import me.geowhat.craftylang.interpreter.error.ModuleError;
import me.geowhat.craftylang.interpreter.error.RuntimeError;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CraftScript {

    public static final int SUCCESS_CODE = 0;
    public static final int PARSE_ERROR_CODE = 1;
    public static final int RUNTIME_ERROR_CODE = 2;
    public static final int USER_REQUEST_CODE = 3;

    public static boolean running = false;

    private static boolean hadError;
    private static boolean hadRuntimeError;
    public static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private static Interpreter interpreter;

    public static void init() {

        // TODO: make this better pls
        String mathCode =
                """
                        let PI = 3.14159265358979;

                        fn min(a, b) {
                          if (a > b) {
                            ret b;
                          }
                          ret a;
                        }

                        fn max(a, b) {
                          if (a > b) {
                            ret a;
                          }
                          ret b;
                        }

                        fn abs(a) {
                          if (a > 0 | a == 0) {
                            ret a;
                          }
                          ret -a;
                        }

                        fn avg(a, b) {
                          ret (a + b) / 2;
                        }

                        fn sqrt(n) {
                          let l = min(1, n);
                          let h = max(1, n);
                          let m = 0;

                          while(100*l*l<n) {
                            l = l * 10;
                          }
                          while (0.01*h*h>n) {
                            h = h * 0.1;
                          } \s
                          for(let i=0;i<100;i=i+1) {
                            m = (l+h)/2;
                            if (m*m==n) {
                              ret m;
                            } if (m*m > n) {
                              h = m;
                            } else {
                              l = m;
                            }
                          }\s
                          ret m;   \s
                        }

                        fn pow(a, e) {
                          if (e == 0 & a == 0) {
                            ret 1;
                          } if (e == 1) {
                            ret a;
                          } if (e < 0) {
                            ret pow(a, -e);
                          } else {
                            let r = 1;
                            while (e > 0) {
                              r = r * a;
                              e = e - 1;
                            }
                            ret r;
                          }
                        }

                        fn hypot(a, b) {
                          ret sqrt(pow(a, 2) + pow(b, 2));
                        }""";
        Modules.modules.put("math", mathCode);
    }

    public static void run(String src) {
        Scheduler.startExecution();

        hadError = false;
        hadRuntimeError = false;

        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();
        interpreter = new Interpreter();

        if (hadError) {
            Message.sendError("Error while parsing");
            interpreter.exitInterpreter(PARSE_ERROR_CODE);
            return;
        }

        running = true;
        interpreter.interpret(statements);

        if (hadRuntimeError) {
            Message.sendError("Exited due to a Runtime Error");
            interpreter.exitInterpreter(RUNTIME_ERROR_CODE);
            return;
        }

        if (running)
            interpreter.exitInterpreter(SUCCESS_CODE);
    }

    public static void kill(int code) {
        if (interpreter != null) {
            interpreter.exitInterpreter(code);
            running = false;
        }
    }

    public static void error(Token token, String message) {
        if (token.type() == TokenType.END)
            report(token.page(), token.line(), "at end", message);
        else
            report(token.page(), token.line(), "at \"" + token.lexeme() + "\"", message);
    }

    public static void error(int page, int line, String message) {
        report(page, line, "", message);
    }

    public static void moduleError(ModuleError error) {
        Message.sendError("CraftScript ModuleError: " + error.getMessage());
        Message.sendError("           unknown module: " + error.module);
        kill(1);
    }

    public static void runtimeError(RuntimeError error) {
        if (!hadRuntimeError)
            Message.sendError("CraftScript RuntimeError: " + error.getMessage());
        Message.sendError("           at page: " + error.token.page() + ", line: " + error.token.line());
        Message.sendError("              |  \"" + error.token.lexeme() + "\"");
        hadRuntimeError = true;
    }

    private static void report(int page, int line, String position, String message) {
        if (!hadError)
            Message.sendError("CraftScript Error: " + message);

        Message.sendError("           at page: " + page + ", line: " + line);
        if (!position.isBlank() && !position.isEmpty())
            Message.sendError("              | " + position);

        hadError = true;
    }
}
