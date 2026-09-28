package com.lianpayhub.service.website;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.website.WebsiteMedia;
import com.lianpayhub.repository.WebsiteMediaRepository;
import com.lianpayhub.service.storage.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

@Service
public class WebsiteMediaService {
    private final WebsiteMediaRepository media; private final StorageService storage; private final FileValidator validator;
    public WebsiteMediaService(WebsiteMediaRepository media,StorageService storage,FileValidator validator){this.media=media;this.storage=storage;this.validator=validator;}
    @Transactional(readOnly=true) public List<WebsiteMedia> list(){return media.findAllByOrderByCreatedAtDesc();}
    @Transactional public WebsiteMedia upload(MultipartFile file,String title,String altText,String description){
        AllowedFileType type=validator.validateWebsiteAsset(file);if(type.getCategory()!=FileCategory.IMAGE && type.getCategory()!=FileCategory.DOWNLOAD)throw new BusinessException(ErrorCode.BAD_REQUEST,"不支持的官网资源类型");
        String original=file.getOriginalFilename()==null?"file":file.getOriginalFilename();String extension=original.contains(".")?original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT):"";String key="website/"+(type.getCategory()==FileCategory.DOWNLOAD?"downloads/":"")+UUID.randomUUID()+extension;
        try{storage.store(key,file.getInputStream(),file.getSize(),type.getMimeType());}catch(IOException e){throw new BusinessException(ErrorCode.SERVER_ERROR,"图片读取失败");}
        String url=storage.getDownloadUrl(key,Duration.ofDays(3650));return media.save(new WebsiteMedia(key,url,original,type.getMimeType(),file.getSize(),blank(title,original),altText,description));
    }
    @Transactional public WebsiteMedia update(Long id,String title,String altText,String description){WebsiteMedia item=get(id);item.update(title,altText,description);return media.save(item);}
    @Transactional public void delete(Long id){WebsiteMedia item=get(id);storage.delete(item.getStorageKey());media.delete(item);}
    private WebsiteMedia get(Long id){return media.findById(id).orElseThrow(()->new BusinessException(ErrorCode.NOT_FOUND,"媒体不存在"));}
    private String blank(String value,String fallback){return value==null||value.trim().isEmpty()?fallback:value.trim();}
}
