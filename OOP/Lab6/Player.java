public class Player {
    private final String name;
    private final Board.Piece piece;
    
    public Player(String name, Board.Piece piece) {
        this.name = name;
        this.piece = piece;
    }
    
    public String getName() { 
        return name; 
    }
    
    public Board.Piece getPiece() { 
        return piece; 
    }
} 