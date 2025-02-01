package hotelmanagementSystem;
//initializing a rooms class with protected object
 class Rooms {
    protected int roomNumber;
    protected RoomType roomType;
    protected boolean isAvailable;
    protected float price;
    //creation of constructor with multiple parameters
    public Rooms(int roomNumber, String roomType, float price, boolean isAvailable) {
        this.roomNumber = roomNumber;
        try {
            this.roomType = RoomType.valueOf(roomType.toUpperCase());
        } catch (IllegalArgumentException e) {
            
            this.roomType = RoomType.SingleRoom;  
        }

        this.isAvailable = isAvailable;  
        this.price = price;
    }
    
   //getters and setters
    public int  getroomNumber() {
    	return roomNumber;
    }
    
    
    public RoomType getroomType() {
    	return roomType;
    }
    
    public void setroomType(RoomType roomType) {
    	this.roomType = roomType;
    }
    
    public float  getprice() {
    	return price;
    }
    
    public void setprice(float price) {
    	this.price = price;
    }
    
    public boolean  isAvailable() {
    	return isAvailable;
    }
    public void setAvailable(boolean isAvailable) {
    	this.isAvailable = isAvailable;
    }
    
    //implementing interface class method
    public void bookARoom() {
    	isAvailable = false;
    }
    
    public void CheckOut() {
    	isAvailable = true;
    }
    
    //overriding a method of displaying data that is all formatted in string
    @Override
    public String toString() {
    	return String.format("ROOM NUMBER: %s, \nROOM TYPE: %s, \nROOM PRICE: \nAVAILABLE: %s, \n",
    			roomNumber, roomType, price, isAvailable ? "available" : "Not available");
    }
}
    
    