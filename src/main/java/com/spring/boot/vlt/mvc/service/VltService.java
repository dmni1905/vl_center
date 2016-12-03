package com.spring.boot.vlt.mvc.service;

import com.spring.boot.vlt.config.property.VltSettings;
import com.spring.boot.vlt.mvc.model.entity.User;
import com.spring.boot.vlt.mvc.model.entity.VirtLab;
import com.spring.boot.vlt.mvc.repository.UserRepository;
import com.spring.boot.vlt.mvc.repository.VlRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Optional;
import java.util.Set;

import static java.nio.file.Files.walk;

@Service
public class VltService {
    private final Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    private VltSettings vltSettings;
    @Autowired
    private UserService userService;
    @Autowired
    private VlRepository vlRepository;

    public Set<VirtLab> getVirtList(String userLogin) {
        return userService.getUserVirtLabs(userLogin);
    }

    @Transactional
    public VirtLab addVl(VirtLab vl, String userLogin) {
        final String path = System.getProperty("user.dir") + File.separator + vltSettings.getPathsUploadedFiles();
        File vlDir = new File(path, "lab" + System.currentTimeMillis());
        while (vlDir.exists()) {
            vlDir = new File(path, "lab" + System.currentTimeMillis());
        }
        if (!vlDir.exists()) {
            vlDir.mkdirs();
            logger.info("Create " + vlDir.getAbsolutePath());
        }
        vl.setDirName(vlDir.getName());
        vl.updatePropertyFile(path);
        vlRepository.save(vl);
        User user = userService.getUserByLogin(userLogin);
        if (!user.getLabs().contains(vl)) {
            user.addLab(vl);
            userService.saveUser(user);
        }
        logger.info("Virtual laboratory " + vl.getDirName() + "create!");
        return vl;
    }

    public VirtLab getPropertyVl(String vlDir, String userLogin) {
        return userService.foundVlUnderUser(userLogin, getPropertyVl(vlDir));
    }

    public VirtLab getPropertyVl(String vlDir) {
        return Optional.ofNullable(vlRepository.findByDirName(vlDir)).orElseThrow(() ->
                new NullPointerException("vl with dir name = " + vlDir + " not found"));
    }

    @Transactional
    public VirtLab savePropertyVl(VirtLab vl, String dir, String userLogin) {
        User user = userService.getUserByLogin(userLogin);
        if (user.getLabs().stream().filter(vl::equals).findFirst().isPresent()) {
            return savePropertyVl(vl, dir);
        } else {
            throw new NullPointerException("User with login = " + userLogin + " not contain vl = " + vl.toString());
        }
    }

    public VirtLab savePropertyVl(VirtLab vl, String dir) {
        final String path = System.getProperty("user.dir") + File.separator + vltSettings.getPathsUploadedFiles();
        VirtLab vlFromDB = Optional.ofNullable(vlRepository.findByDirName(dir)).orElseThrow(() ->
                new NullPointerException("vl with dir name = " + dir + " not found"));
        vlFromDB.setName(vl.getName());
        vlFromDB.setHeight(vl.getHeight());
        vlFromDB.setWidth(vl.getWidth());
        vl.updatePropertyFile(path);
        return vlRepository.save(vlFromDB);
    }

    public byte[] getImg(String dir, String name, String suffix) throws IOException {
        final String path = System.getProperty("user.dir") + File.separator + vltSettings.getPathsUploadedFiles();
        final File img = new File(path + File.separator + dir + File.separator + "tool" + File.separator + "img", name + "." + suffix);
        return getStatic(img);
    }


    public byte[] getImg2(String dir, String name, String suffix) throws IOException {
        final String path = System.getProperty("user.dir") + File.separator + vltSettings.getPathsUploadedFiles();
        final File img = new File(path + File.separator + dir + File.separator + "tool" + File.separator + "img", name + "." + suffix);
        return getStatic(img);
    }

    private byte[] getStatic(File img) throws IOException {
        if (img.exists()) {
            RandomAccessFile f = new RandomAccessFile(img, "r");
            byte[] b = new byte[(int) f.length()];
            f.readFully(b);
            return b;
        } else {
            logger.error("File " + img.getAbsolutePath() + " not found!");
            return null;
        }

    }
}
