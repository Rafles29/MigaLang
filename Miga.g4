grammar Miga;

prog: block
;

block: ( stat? NEWLINE )*
;

stat: declare
    | assign
    | declare_and_assign
    | tab_lang
	| system_func
	| loop
	| if_statement
;
if_block: block
;

if_statement: IF conditional BEGIN if_block END
;

function: FUNCTION WS ID WS LEFT_ROUND_BRACKET fparam RIGHT_ROUND_BRACKET WS BEGIN block END
;

fparam: ID
;

loop: LOOP WS  conditional WS BEGIN block END
;


conditional: expr EQUAL_SIGN expr #equal
    | expr NOT_EQUAL_SIGN expr #notequal
    | expr LESS_SIGN expr #less
    | expr LESS_EQUAL_SIGN #lessequal
    | expr MORE_SIGN #more
    | expr MORE_EQUAL_SIGN #moreequal
;

declare: TYPE_NAME ID
;

assign: ID ASSIGN expr
    | ID ASSIGN STRING
;

declare_and_assign: TYPE_NAME assign
;

tab_lang: declare_tab
    | get_tab_val
    | set_tab_val
    | print_tab
;

declare_tab: TYPE_NAME LEFT_BRACKET INT RIGHT_BRACKET ID
;

get_tab_val: ID LEFT_BRACKET INT RIGHT_BRACKET
;

set_tab_val: get_tab_val ASSIGN expr
;

print_tab: PRINT get_tab_val
;

system_func: print
    | read
;

print: PRINT ID
    | PRINT STRING
;

read: READ ID
;

expr:  arithemtic_expr
;

arithemtic_expr:  element   #single_value
    | element ADD element   #add
    | element SUBTRACT element  #subtract
    | element MULT element  #mult
    | element DIVIDE element    #divide
    | '(' expr ')'  #par
;

element:   INT          #int
    | REAL              #real
    | TOINT element     #toint
    | TOREAL element    #toreal
    | ID                #id
    | get_tab_val #gettabval
;

TYPE_NAME: 'int'
    | 'float'
    | 'string'
;

PRINT:	'print'
;

READ:	'read'
;

FUNCTION: 'func'
;

LOOP: 'while'
;

IF:	'if'
;

BEGIN: '{'
;

END: '}'
;

LEFT_ROUND_BRACKET: '('
;

RIGHT_ROUND_BRACKET: ')'
;

LEFT_BRACKET: '['
;

RIGHT_BRACKET: ']'
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

ADD: '+'
;

SUBTRACT: '-'
;

MULT: '*'
;

DIVIDE: '/'
;

ASSIGN: '='
;

EQUAL_SIGN: '=='
;

NOT: '!'
;

NOT_EQUAL_SIGN: '!='
;

LESS_SIGN: '<'
;

LESS_EQUAL_SIGN: '<='
;

MORE_SIGN: '>'
;

MORE_EQUAL_SIGN: '>='
;

NEWLINE:	'\r'? '\n'
;

WS:   (' '|'\t')+ { skip(); }
;
