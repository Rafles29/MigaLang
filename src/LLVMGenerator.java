import java.util.Stack;

class LLVMGenerator{
   
   static String header_text = "";
   static String main_text = "";
   static int reg = 1;
   static int br = 0;

   static Stack<Integer> brstack = new Stack<>();

   static void ifstart(){
      br++;
      main_text += "br i1 %"+(reg-1)+", label %true"+br+", label %false"+br+"\n";
      main_text += "true"+br+":\n";
      brstack.push(br);
   }

   static void ifend(){
      int b = brstack.pop();
      main_text += "br label %false"+b+"\n";
      main_text += "false"+b+":\n";
   }

   static void while_start() {
      br++;
      main_text += "br label %cond"+br+"\n";
      main_text += "cond"+br+":\n";
   }

   static void while_cond_end() {
      main_text += "br i1 %"+(reg-1)+", label %true"+br+", label %false"+br+"\n";
      main_text += "true"+br+":\n";
      brstack.push(br);
   }

   static void while_end(){
      int b = brstack.pop();
      main_text += "br label %cond"+b+"\n";
      main_text += "false"+b+":\n";
   }

   static void equal_i32(String val1, String val2){
      main_text += "%"+reg+" = icmp eq i32 "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void notequal_i32(String val1, String val2){
      main_text += "%"+reg+" = icmp ne i32 "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void less_i32(String val1, String val2){
      main_text += "%"+reg+" = icmp slt i32 "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void more_i32(String val1, String val2){
      main_text += "%"+reg+" = icmp sgt i32 "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void less_equal_i32(String val1, String val2){
      main_text += "%"+reg+" = icmp sle i32 "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void more_equal_i32(String val1, String val2){
      main_text += "%"+reg+" = icmp sge i32 "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void equal_double(String val1, String val2){
      main_text += "%"+reg+" = fcmp oeq double "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void notequal_double(String val1, String val2){
      main_text += "%"+reg+" = fcmp une double "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void less_double(String val1, String val2){
      main_text += "%"+reg+" = fcmp olt double "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void more_double(String val1, String val2){
      main_text += "%"+reg+" = fcmp ogt double "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void less_equal_double(String val1, String val2){
      main_text += "%"+reg+" = fcmp ole double "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void more_equal_double(String val1, String val2){
      main_text += "%"+reg+" = fcmp oge double "+(val2)+", "+val1+"\n";
      reg++;
   }

   static void printf_i32(String id){
      load_i32(id);
      main_text += "%"+reg+" = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %"+(reg-1)+")\n";
      reg++;
   }

   static void printf_double(String id){
      load_double(id);
      main_text += "%"+reg+" = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %"+(reg-1)+")\n";
      reg++;
   }

   static void scanf_i32(String id){
      main_text += "%"+reg+" = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strsi, i32 0, i32 0), i32* %"+id+")\n";
      reg++;
   }

   static void scanf_double(String id){
      main_text += "%"+reg+" = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strsd, i32 0, i32 0), double* %"+id+")\n";
      reg++;
   }

   static void declare_i32(String id){
      main_text += "%"+id+" = alloca i32\n";
   }

   static void declare_double(String id){
      main_text += "%"+id+" = alloca double\n";
   }

   static void assign_i32(String id, String value){
      main_text += "store i32 "+value+", i32* %"+id+"\n";
   }

   static void assign_double(String id, String value){
      main_text += "store double "+value+", double* %"+id+"\n";
   }


   static void load_i32(String id){
      main_text += "%"+reg+" = load i32, i32* %"+id+"\n";
      reg++;
   }

   static void load_double(String id){
      main_text += "%"+reg+" = load double, double* %"+id+"\n";
      reg++;
   }

   static void add_i32(String val1, String val2){
      main_text += "%"+reg+" = add i32 "+val1+", "+val2+"\n";
      reg++;
   }

   static void sub_i32(String val1, String val2){
      main_text += "%"+reg+" = sub i32 "+val2+", "+val1+"\n";
      reg++;
   }

   static void add_double(String val1, String val2){
      main_text += "%"+reg+" = fadd double "+val1+", "+val2+"\n";
      reg++;
   }

   static void sub_double(String val1, String val2){
      main_text += "%"+reg+" = fsub double "+val2+", "+val1+"\n";
      reg++;
   }

   static void mult_i32(String val1, String val2){
      main_text += "%"+reg+" = mul i32 "+val1+", "+val2+"\n";
      reg++;
   }

   static void mult_double(String val1, String val2){
      main_text += "%"+reg+" = fmul double "+val1+", "+val2+"\n";
      reg++;
   }

   static void div_i32(String val1, String val2){
      main_text += "%"+reg+" = div i32 "+val2+", "+val1+"\n";
      reg++;
   }

   static void div_double(String val1, String val2){
      main_text += "%"+reg+" = fdiv double "+val2+", "+val1+"\n";
      reg++;
   }

   static void declare_tab_i32(String id, String nmbOfElements) {
      main_text += "%"+id+" =  alloca ["+nmbOfElements+" x i32]\n";
   }

   static void declare_tab_double(String id, String nmbOfElements) {
      main_text += "%"+id+" =  alloca ["+nmbOfElements+" x double]\n";
   }

   static void load_tab_i32(String id, String element, String len){
      main_text += "%"+reg+" = getelementptr inbounds ["+len+" x i32], ["+len+" x i32]* %"+id+", i64 0, i64 "+element+"\n";
      reg++;
   }

   static void load_tab_double(String id, String element, String len){
      main_text += "%"+reg+" = getelementptr inbounds ["+len+" x double], ["+len+" x double]* %"+id+", i64 0, i64 "+element+"\n";
      reg++;
   }

   static void fptosi(String id){
      main_text += "%"+reg+" = fptosi double "+id+" to i32\n";
      reg++;
   }

   static void sitofp(String id){
      main_text += "%"+reg+" = sitofp i32 "+id+" to double\n";
      reg++;
   }

   static String generate(){
      String text = "";
      text += "declare i32 @printf(i8*, ...)\n";
      text += "declare i32 @scanf(i8*, ...)\n";
      text += "@strpi = constant [4 x i8] c\"%d\\0A\\00\"\n";
      text += "@strpd = constant [4 x i8] c\"%f\\0A\\00\"\n";
      text += "@strsi = constant [3 x i8] c\"%d\\00\"\n";
      text += "@strsd = constant [4 x i8] c\"%lf\\00\"\n";
      text += header_text;
      text += "define i32 @main() nounwind{\n";
      text += main_text;
      text += "ret i32 0 }\n";
      return text;
   }

}
