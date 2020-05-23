// Intro to ANTLR+LLVM
// sawickib, 2014-04-26

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String source = "test.miga";
        ANTLRFileStream input = new ANTLRFileStream(source);

        MigaLexer lexer = new MigaLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MigaParser parser = new MigaParser(tokens);

        ParseTree tree = parser.prog(); 

        //System.out.println(tree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new MigaActions(), tree);

    }
}
