package com.gitee.dorive.launcher.v1;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Test {

    public static void main(String[] args) {
        String inputPath1 = "C:\\Users\\Administrator\\Desktop\\Cloud\\dorive";
        Set<String> filenames1 = new LinkedHashSet<>();
        resolve(inputPath1, filenames1);

        String inputPath2 = "C:\\Users\\Administrator\\Desktop\\Cloud\\test\\dorive";
        Set<String> filenames2 = new LinkedHashSet<>();
        resolve(inputPath2, filenames2);

        filenames2.removeAll(filenames1);
        System.out.println("==================== 差集 ======================");
        for (String filename : filenames2) {
            System.out.println(filename);
        }
    }

    private static void resolve(String inputPath, Set<String> filenames) {
        List<Path> paths = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(Paths.get(inputPath))) {
            walk.filter(Files::isRegularFile).filter(path -> {
                String filename = path.getFileName().toString();
                return filename.endsWith(".java");
            }).forEach(paths::add);

        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Path path : paths) {
            String fileName = path.getFileName().toString();
            filenames.add(fileName);
        }
        System.out.println("总数：" + filenames.size());
        for (String filename : filenames) {
            System.out.println(filename);
        }
    }

}
