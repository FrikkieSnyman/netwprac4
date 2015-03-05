import java.io.Serializable;

public class Friend implements Serializable{

    private String name;
    private String number;
    private String photo;

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

    public void setPhoto(String photo){
        this.photo = photo;
    }

    public String getName(){
        return name;
    }

    public String getNumber(){
        return number;
    }

    public String getString(){
        return photo;
    }

    @Override
    public String toString(){
        return new StringBuffer("Name: ").append(this.name).append("\t\tNumber: ").append(this.number).toString();
    }
}