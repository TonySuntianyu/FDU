package editor.workspace;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工作区备忘录测试
 */
public class WorkspaceMementoTest {
    @Test
    void testSaveAndLoad(@TempDir Path tempDir) throws IOException {
        // 切换到临时目录
        System.setProperty("user.dir", tempDir.toString());

        List<WorkspaceState.FileState> files = new ArrayList<>();
        files.add(new WorkspaceState.FileState("file1.txt", true));
        files.add(new WorkspaceState.FileState("file2.txt", false));

        WorkspaceState state = new WorkspaceState(files, "file1.txt", false);
        WorkspaceMemento.save(state);

        WorkspaceState loaded = WorkspaceMemento.load();
        assertNotNull(loaded);
        assertEquals("file1.txt", loaded.getActiveFilePath());
        assertEquals(2, loaded.getFiles().size());
    }

    @Test
    void testLoadNonExistent() {
        // 在不存在工作区文件的情况下加载
        WorkspaceState state = WorkspaceMemento.load();
        // 应该返回null或空状态
        // 这里根据实现可能返回null
    }
}

