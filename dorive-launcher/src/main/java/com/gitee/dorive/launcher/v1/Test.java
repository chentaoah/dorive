/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        String inputPath1 = "D:\\Work\\CloudSpace\\dorive";
        Set<String> filenames1 = new LinkedHashSet<>();
        resolve(inputPath1, filenames1);

        String inputPath2 = "C:\\Users\\chenT\\Desktop\\new\\dorive";
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
