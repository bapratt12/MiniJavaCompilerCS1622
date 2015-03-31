/* 
bap74
CS1622
Project 3 - A MiniJava Compiler
*/

import java_cup.runtime.Symbol;

%%

%class MiniJavaLexer
%cup
%implements sym

%unicode

%line
%column

%{
StringBuffer string = new StringBuffer();

private Symbol symbol(int type){
	return new Symbol(type, yyline, yycolumn);
}

private Symbol symbol(int type, Object value){
	return new Symbol(type, yyline, yycolumn, value);
}

%}

%eofval{
    return new Symbol(sym.EOF);
%eofval}

Integer = 0 | -?[1-9][0-9]*

//input_char = [^\r\n]

new_line = \r|\n|\r\n|\z;
white_space = {new_line} | [ \t\f]


id = [a-zA-Z][a-zA-Z0-9_]*

block_comment = "/*"~"*/"
line_comment = "//"~{new_line}



%%
<YYINITIAL>{
/* operators */

"+"			{ return symbol(PLUS); }
"-"			{ return symbol(MINUS); }
"*" 		{ return symbol(TIMES); }
"="			{ return symbol(EQUALS); }
"&&"		{ return symbol(AND); }
"<"			{ return symbol(LESS_THAN); }
"!"			{ return symbol(NOT); }

/* keywords */

"boolean"	{ return symbol(BOOLEAN); }
"class"		{ return symbol(CLASS); }
"else"		{ return symbol(ELSE); }
"extends"	{ return symbol(EXTENDS); }
"if"		{ return symbol(IF); }
"int"		{ return symbol(INT); }
"length"    { return symbol(LENGTH); }
"main"		{ return symbol(MAIN); }
"new" 		{ return symbol(NEW); }
"public" 	{ return symbol(PUBLIC); }
"return"	{ return symbol(RETURN); }
"static" 	{ return symbol(STATIC); }
"String"	{ return symbol(STRING); }
"System.out.println"	{ return symbol(PRINT); }
"this" 		{ return symbol(THIS); }
"void" 		{ return symbol(VOID); }
"while"     { return symbol(WHILE); }

/* boolean values */
"true"		{ return symbol(TRUE); }
"false"		{ return symbol(FALSE); }

/* separators */
"("			{ return symbol(LPAREN); }
")"			{ return symbol(RPAREN); }
"["			{ return symbol(LBRACK); }
"]"			{ return symbol(RBRACK); }
"{"			{ return symbol(LCURLY); }
"}"			{ return symbol(RCURLY); }
","			{ return symbol(COMMA); }
"."			{ return symbol(DOT); }
";"			{ return symbol(SEMICOLON); }

/* comments */
{line_comment}	{ /* ignore */ }
{block_comment}	{ /* ignore */ }

/* white space */
{white_space}	{ /* ignore */ }

/* literals */
{Integer}	{ return symbol(INTEGER_LITERAL, Integer.parseInt(yytext())); }

/* identifiers */
{id}		{ return symbol(IDENTIFIER, yytext()); }

} /* end YYINITIAL */



/* strings */
/*
<STRING> {
  \"                             { yybegin(YYINITIAL); 
                                   return symbol(sym.STRING_LITERAL, 
                                   string.toString()); }
  [^\n\r\"\\]+                   { string.append( yytext() ); }
  \\t                            { string.append('\t'); }
  \\n                            { string.append('\n'); }
  [ ]                            { string.append( yytext() ); }
  \\r                            { string.append('\r'); }
  \\\"                           { string.append('\"'); }
  \\                             { string.append('\\'); }
}
*/



