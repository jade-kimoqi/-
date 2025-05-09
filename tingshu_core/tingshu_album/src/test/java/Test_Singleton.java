public class Test_Singleton {
    private Test_Singleton(){}
    private volatile static Test_Singleton instance;
    public static Test_Singleton getInstance(){
        if(instance == null){
            synchronized (Test_Singleton.class){
                if(instance==null){
                    instance = new Test_Singleton();
                }
            }
        }
        return instance;
    }

}
