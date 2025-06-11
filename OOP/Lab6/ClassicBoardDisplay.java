/**
 * 传统8x8棋盘显示类，用于和平模式和黑白棋模式
 */
public class ClassicBoardDisplay implements BoardDisplay {
    @Override
    public void displayBoard(Board board, Player currentPlayer, Player player1, Player player2,
                           GameMode mode, int boardIndex, BoardManager boardManager) {
        // 显示当前模式和棋盘编号
        String modeName = "";
        if (mode instanceof PeaceMode) {
            modeName = "peace";
        } else if (mode instanceof ReversiMode) {
            modeName = "reversi";
        }
        System.out.printf("当前模式: %s (棋盘编号: %d)\n\n", modeName, boardIndex);

        // 显示8x8棋盘
        System.out.println("  a b c d e f g h");
        for (int i = 0; i < 8; i++) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < 8; j++) {
                if (board.getPiece(i, j) == Board.Piece.BLACK) {
                    System.out.print("● ");
                } else if (board.getPiece(i, j) == Board.Piece.WHITE) {
                    System.out.print("○ ");
                } else {
                    if (mode instanceof ReversiMode &&
                        ((ReversiMode)mode).isValidMove(board, i, j, currentPlayer.getPiece())) {
                        System.out.print("+ ");
                    } else {
                        System.out.print("· ");
                    }
                }
            }

            System.out.print("   ");  // 3个空格作为基本间距

            // 右侧信息显示
            if (i == 3) {
                String mark = (currentPlayer == player1) ? "●" : " ";
                int[] score = board.getScore();
                if (mode instanceof PeaceMode) {
                    System.out.printf("玩家[%s] %s                    1号棋盘: peace", 
                        player1.getName(), mark);
                } else {
                    System.out.printf("玩家[%s] %s  得分：%-4d        1号棋盘: peace", 
                        player1.getName(), mark, score[0]);
                }
            } else if (i == 4) {
                String mark = (currentPlayer == player2) ? "○" : " ";
                int[] score = board.getScore();
                if (mode instanceof PeaceMode) {
                    System.out.printf("玩家[%s] %s                    2号棋盘: reversi", 
                        player2.getName(), mark);
                } else {
                    System.out.printf("玩家[%s] %s  得分：%-4d        2号棋盘: reversi", 
                        player2.getName(), mark, score[1]);
                }
            } else if (i == 5) {
                // 分别处理3号棋盘的显示
                GameMode thirdBoardMode = boardManager.getModeAtIndex(2);
                String thirdBoardType = "";
                if (thirdBoardMode instanceof PeaceMode) {
                    thirdBoardType = "peace";
                } else if (thirdBoardMode instanceof ReversiMode) {
                    thirdBoardType = "reversi";
                } else if (thirdBoardMode instanceof GomokuMode) {
                    thirdBoardType = "gomoku";
                }
                
                System.out.printf("                                3号棋盘: %s", thirdBoardType);
            } else if (i > 5 && i < 5 + boardManager.getBoardCount() - 2) {
                int displayBoardNum = i - 5 + 3;  // 从3号棋盘开始
                if (displayBoardNum <= boardManager.getBoardCount()) {
                    GameMode boardMode = boardManager.getModeAtIndex(displayBoardNum - 1);
                    if (boardMode != null) {
                        String boardTypeName = "";
                        if (boardMode instanceof PeaceMode) {
                            boardTypeName = "peace";
                        } else if (boardMode instanceof ReversiMode) {
                            boardTypeName = "reversi";
                        } else if (boardMode instanceof GomokuMode) {
                            boardTypeName = "gomoku";
                        }
                        System.out.printf("                                %d号棋盘: %s",
                            displayBoardNum, boardTypeName);
                    }
                }
            }
            System.out.println();
        }
        System.out.println("\n");
    }
} 