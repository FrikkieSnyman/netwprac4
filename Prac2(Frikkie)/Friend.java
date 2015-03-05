import java.io.Serializable;

public class Friend implements Serializable{

    private String name;
    private String number;

    public Friend(){

    }

    public Friend(String name, String number){
        this.name = name;
        this.number = number;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setNumber(String number){
        this.number = number;
    }

    public String getName(){
        return name;
    }

    public String getNumber(){
        return number;
    }

    @Override
    public String toString(){
        return new StringBuffer("Name: ").append(this.name).append("\t\tNumber: ").append(this.number).toString();
    }
}