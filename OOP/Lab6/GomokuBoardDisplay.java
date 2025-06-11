/**
 * 五子棋15x15棋盘显示类
 */
public class GomokuBoardDisplay implements BoardDisplay {
    @Override
    public void displayBoard(Board board, Player currentPlayer, Player player1, Player player2,
                           GameMode mode, int boardIndex, BoardManager boardManager) {
        if (!(mode instanceof GomokuMode)) {
            // 如果不是五子棋模式，不执行任何操作
            return;
        }

        // 显示当前模式和棋盘编号
        System.out.printf("当前模式: gomoku (棋盘编号: %d)\n\n", boardIndex);

        // 显示15x15棋盘（列表头：A-O）
        System.out.print("   ");
        for (int j = 0; j < 15; j++) {
            System.out.print((char)('A' + j) + " ");
        }
        System.out.println();
        
        // 显示棋盘内容（行号：1-F，16进制）
        for (int i = 0; i < 15; i++) {
            // 使用16进制表示行号 (1-F)
            System.out.printf("%2X ", i + 1);
            
            for (int j = 0; j < 15; j++) {
                Board.Piece piece = board.getPiece(i, j);
                if (piece == Board.Piece.BLACK) {
                    System.out.print("● ");
                } else if (piece == Board.Piece.WHITE) {
                    System.out.print("○ ");
                } else if (piece == Board.Piece.BARRIER) {
                    System.out.print("# ");
                } else if (piece == Board.Piece.CRATER) {
                    System.out.print("@ ");
                } else {
                    System.out.print("· ");
                }
            }
            
            System.out.print("   "); // 3个空格作为基本间距
            
            // 右侧信息显示
            if (i == 3) {
                String mark = (currentPlayer == player1) ? "●" : " ";
                int[] score = board.getScore();
                System.out.printf("玩家[%s] %s  得分：%-4d", 
                    player1.getName(), mark, score[0]);
                
                // 显示黑棋玩家炸弹数量
                GomokuMode gomokuMode = (GomokuMode)mode;
                System.out.printf("  炸弹：%d", gomokuMode.getBlackBombs());
            } else if (i == 4) {
                String mark = (currentPlayer == player2) ? "○" : " ";
                int[] score = board.getScore();
                System.out.printf("玩家[%s] %s  得分：%-4d", 
                    player2.getName(), mark, score[1]);
                
                // 显示白棋玩家炸弹数量
                GomokuMode gomokuMode = (GomokuMode)mode;
                System.out.printf("  炸弹：%d", gomokuMode.getWhiteBombs());
            } else if (i == 5) {
                // 显示炸弹用法说明
                System.out.print("使用炸弹：输入@位置(如@FA)");
            } else if (i == 6) {
                // 显示棋盘信息
                System.out.print("1号棋盘: peace");
            } else if (i == 7) {
                System.out.print("2号棋盘: reversi");
            } else if (i == 8) {
                GameMode thirdBoardMode = boardManager.getModeAtIndex(2);
                String thirdBoardType = "gomoku";
                if (thirdBoardMode instanceof PeaceMode) {
                    thirdBoardType = "peace";
                } else if (thirdBoardMode instanceof ReversiMode) {
                    thirdBoardType = "reversi";
                }
                System.out.printf("3号棋盘: %s", thirdBoardType);
            } else if (i >= 9 && i < 9 + boardManager.getBoardCount() - 3) {
                int displayBoardNum = i - 9 + 4;  // 从4号棋盘开始
                if (displayBoardNum <= boardManager.getBoardCount()) {
                    GameMode boardMode = boardManager.getModeAtIndex(displayBoardNum - 1);
                    if (boardMode != null) {
                        String boardTypeName = "gomoku";
                        if (boardMode instanceof PeaceMode) {
                            boardTypeName = "peace";
                        } else if (boardMode instanceof ReversiMode) {
                            boardTypeName = "reversi";
                        }
                        System.out.printf("%d号棋盘: %s", displayBoardNum, boardTypeName);
                    }
                }
            }
            System.out.println();
        }
        System.out.println("\n");
    }
} 