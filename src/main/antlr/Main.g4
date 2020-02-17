grammar Main;

prog:	( stmt | funcDecl | funcCall ';' )* ;
stmt:   varDecl
    |   varAssign
    |   ifElse
    |   returnStmt
    |   printStmt
    ;

returnStmt: 'return' expr? ';' ;
funcDecl:   'func' ID '(' paramList ')' funcBody ;
funcBody:   '{' ( stmt | funcCall )* '}' ;
funcCall:   ID '(' argList ')' ;

paramList:  (ID ',')* ID? ;
argList:    (expr ',')* expr? ;

varDecl:    'var' varAssign ;
varAssign:  (ID '=')+ expr ';';

ifElse:  'if' '(' expr ')' block
      |  'if' '(' expr ')' block 'else' block
      ;

block:  '{' ( stmt | funcCall )* '}' ;

expr: intExpr
    | idExpr
    | funcCall
    | parenExpr
    | expr op=('==' | '!=' | '<' | '>' | '<=' | '>=') expr
    | expr op=('*' | '/' | '+' | '-') expr
    ;

idExpr: ID ;
intExpr: INT ;
parenExpr: '(' expr ')';

/*expr: (idExpr | intExpr | funcCall | parenExpr) compExpr
    | (idExpr | intExpr | funcCall | parenExpr) arithExpr
    | funcCall
    | intExpr
    | idExpr
    | parenExpr
    ;

compExpr:  op=('==' | '!=' | '<' | '>' | '<=' | '>=') expr | ;
arithExpr: op=('*' | '/' | '+' | '-') expr | ;
*/

printStmt: 'print' '(' expr? ')' ';';

NEWLINE :'\r'? '\n' -> skip;
WS      : (' '|'\t') -> skip;
INT     : [0-9]+ ;
ID      : [A-Za-z_]+[A-Za-z_0-9]* ;