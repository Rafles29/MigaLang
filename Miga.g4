// Demo for two types expression grammar
// Bartosz Sawicki, 2014-05-12

grammar Miga;

prog: ( stat? NEWLINE )*
;

stat: declare
    | assign
    | declare_and_assign
	| system_func
;

declare: TYPE_NAME ID
;

assign: ID ASSIGN expr
    | ID ASSIGN STRING
;

declare_and_assign: TYPE_NAME assign
;

system_func: print
    | read
;

print: PRINT ID
    | PRINT STRING
;

read: READ ID
;

expr:  arithemtic_expr #single0
;

arithemtic_expr:  value_type			#single_value
    | value_type ADD value_type		#add
    | value_type SUBTRACT value_type #subtract
    | value_type MULT value_type	#mult
    | value_type DIVIDE value_type	#divide
    | '(' expr ')'		#par
;

value_type:   INT			#int
    | REAL			#real
    | TOINT value_type		#toint
    | TOREAL value_type		#toreal
       
;

TYPE_NAME: 'int' WS
    | 'float' WS
    | 'string' WS
;

PRINT:	'print' WS
;

READ:	'read' WS
;

TOINT: '(int)'
;

TOREAL: '(real)'
;

ID:   ('a'..'z'|'A'..'Z')+
;

REAL: '0'..'9'+'.''0'..'9'+
;

INT: '0'..'9'+
;

STRING: '"' ( ~('\\'|'"') )* '"'
;

ADD: WS '+' WS
;

SUBTRACT: WS '-' WS
;

MULT: WS '*' WS
;

DIVIDE: WS '/' WS
;

ASSIGN: WS '=' WS
;

NEWLINE:	'\r'? '\n'
;

WS:   (' '|'\t')+ { skip(); }
;
