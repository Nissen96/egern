grammar Main;

prog:   ( stmt | funcDecl | classDecl )* ;
stmt:   varDecl ';'?
    |   varAssign ';'?
    |   opAssign ';'?
    |   ifElse
    |   returnStmt ';'?
    |   printStmt ';'?
    |   whileLoop
    |   funcCall ';'?
    |   methodCall ';'?
    ;

returnStmt: 'return' expr? ;
funcDecl:   'func' ID '(' paramList ')' ':' typeDecl '{' funcBody '}'  ;
funcBody:   ( stmt | funcDecl )* ;
funcCall:   ID '(' argList ')' ;

paramList:  (ID ':' typeDecl ',')* (ID ':' typeDecl)? ;
argList:    (expr ',')* expr? ;

assignable: idExpr
          | arrayIndexExpr
          | classField
          ;

varDecl: 'var' (ID '=')+ expr ;
varAssign: (assignable '=')+ expr ;
opAssign: assignable op=('+=' | '-=' | '*=' | '/=' | '%=' ) expr ;

ifElse: 'if' '(' expr ')' block ('else' (block | ifElse))? ;

whileLoop: 'while' '(' expr ')' block ;

block:  '{' stmt* '}' ;

classDecl: 'class' CLASSNAME ('(' paramList ')')? (':' CLASSNAME ('(' argList ')')?)? '{' classBody '}' ;
classBody: (methodDecl | fieldDecl)* ;

methodDecl: funcDecl ;
fieldDecl: varDecl ;
methodCall: (ID | 'this') '.' funcCall ;
classField: (ID | 'this') '.' ID ;
objectInstantiation: CLASSNAME '(' argList ')' ;

arrayIndexExpr: idExpr ('[' expr ']')+ ;

expr: funcCall
    | objectInstantiation
    | methodCall
    | classField
    | arrayIndexExpr
    | op='-' expr
    | op='!' expr
    | expr op=('*' | '/' | '%') expr
    | expr op=('+' | '-') expr
    | expr op=('==' | '!=' | '<' | '>' | '<=' | '>=') expr
    | expr op='&&' expr
    | expr op='||' expr
    | booleanExpr
    | idExpr
    | intExpr
    | arrayExpr
    | parenExpr
    | lenExpr
    ;

idExpr: ID ;
intExpr: INT ;
booleanExpr: BOOLEAN ;
parenExpr: '(' expr ')';
arrayExpr: '[' (expr ',')* expr? ']';
lenExpr: 'len' '(' expr ')';

typeDecl: VOID | PRIMITIVE | arrayType | CLASSNAME ;
arrayType: '[' (arrayType | PRIMITIVE) ']';

printStmt: 'print' '(' expr? ')' ;

NEWLINE  :'\r'? '\n' -> skip;
WS       : (' '|'\t') -> skip;
INT      : [0-9]+ ;
BOOLEAN  : 'true' | 'false' ;
VOID     : 'void' ;
PRIMITIVE: 'int' | 'boolean' ;
ID       : [a-z_]+[A-Za-z_0-9]* ;
CLASSNAME: [A-Z]+[A-Za-z_0-9]* ;
COMMENT  : '/*' .*? '*/' -> skip ;
LINE_COMMENT: '//' ~[\r\n]* -> skip ;

ERROR    : . ;
