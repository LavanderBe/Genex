package Genex.interfaces;

public interface ICrud <T>{
    public void addEntity(T t);
    public void updateEntity(T t,String id);
    public void deleteEntity(T t);
    public void getEntity(T t);
}
