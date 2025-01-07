
package Model;

/**
 *
 * @author MOCIN
 */
public class Customer {
    private int queueNumber;
    private String name;
    private String parcelId;

    public Customer(int queueNumber, String name, String parcelId) {
        this.queueNumber = queueNumber;
        this.name = name;
        this.parcelId = parcelId;
    }

    
        public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty.");
        }
        this.name = name;
    }
 
    public void setQueueNumber(int queueNumber) {
        this.queueNumber = queueNumber;
    }
    
    public int getQueueNumber() {
        return queueNumber;
    }

    public String getName() {
        return name;
    }
    
    

    public String getParcelId() {
        return parcelId;
    }
}

