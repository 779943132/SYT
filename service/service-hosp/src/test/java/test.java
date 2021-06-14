import org.junit.jupiter.api.Test;
public class test {
    @Test
    public void reg(){
        String reg="^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        String keyword="779sada@qq.sda";
        System.out.println(keyword.matches(reg));
    }
}
