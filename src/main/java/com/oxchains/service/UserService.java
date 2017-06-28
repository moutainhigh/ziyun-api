package com.oxchains.service;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.oxchains.bean.model.ziyun.JwtToken;
import com.oxchains.bean.model.ziyun.TabLog;
import com.oxchains.bean.model.ziyun.TabToken;
import com.oxchains.bean.model.ziyun.TabUser;
import com.oxchains.bean.model.ziyun.Token;
import com.oxchains.common.ConstantsData;
import com.oxchains.common.RespDTO;
import com.oxchains.dao.TabLogDao;
import com.oxchains.dao.TabTokenDao;
import com.oxchains.dao.TabUserDao;
import com.oxchains.util.Md5Utils;
import com.oxchains.util.TokenUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class UserService extends BaseService {
	
	@Resource
	private ChaincodeService chaincodeService;
	
	@Resource
	private TabUserDao tabUserDao;
	
	@Resource
	private TabTokenDao tabTokenDao;
	
	@Resource
	private TabLogDao tabLogDao;

	public RespDTO<String> addUser(String body){
		TabUser user = new TabUser();
		try{
			JsonObject obj = gson.fromJson(body, JsonObject.class);
			user.setUsername(obj.get("username").getAsString());
			user.setPassword(Md5Utils.getMD5(obj.get("password").getAsString()));
			user.setRealname(obj.get("realname").getAsString());
			user.setStatus("1");
			
			String username = user.getUsername();
			String password = user.getPassword();
			String realname = user.getRealname();
			log.debug(username+"="+password+"="+realname);
			TabUser userEntity = tabUserDao.findByUsername(username);
			if(userEntity != null && StringUtils.isNotEmpty(userEntity.getUsername())){
				return RespDTO.fail("操作失败",ConstantsData.RTN_DATA_ALREADY_EXISTS);
			}
			
			user = tabUserDao.save(user);
			
			if(user != null && user.getId()>0){
				log.debug("===userid==="+user.getId());
				String txID = chaincodeService.invoke("add", new String[]{username});
				log.debug("===txID==="+txID);
				if(txID == null){
					return RespDTO.fail("操作失败",ConstantsData.RTN_SERVER_INTERNAL_ERROR);
				}
				return RespDTO.success("操作成功", gson.toJson(user));
			}
		}catch(JsonSyntaxException e){
			log.error(e.getMessage());
			return RespDTO.fail("操作失败",ConstantsData.RTN_INVALID_ARGS);
		}
		catch(NullPointerException e){
			log.error(e.getMessage());
			return RespDTO.fail("操作失败",ConstantsData.RTN_INVALID_ARGS);
		}
		catch(Exception e){
			log.error(e.getMessage());
			return RespDTO.fail("操作失败",ConstantsData.RTN_SERVER_INTERNAL_ERROR);
		}
		return RespDTO.success("操作成功", gson.toJson(user));
	}
	
	public RespDTO<String> login(String body){
		TabUser user = new TabUser();
		try{
			JsonObject obj = gson.fromJson(body, JsonObject.class);
			user.setUsername(obj.get("username").getAsString());
			user.setPassword(obj.get("password").getAsString());
		}catch(JsonSyntaxException | NullPointerException e){
			log.error(e.getMessage());
			return RespDTO.fail("操作失败",ConstantsData.RTN_INVALID_ARGS);
		}
		String username = user.getUsername();
		String password = user.getPassword();
		try{
			log.debug(username+"="+password);
			//verify username and password
			String md5pwd = Md5Utils.getMD5(password);
			TabUser userEntity = tabUserDao.findByUsername(username);
			if(userEntity != null){
				if(!md5pwd.equals(userEntity.getPassword())){
					return RespDTO.fail("操作失败",ConstantsData.RTN_USERNAMEORPWD_ERROR);
				}
			}
			else{
				return RespDTO.fail("操作失败",ConstantsData.RTN_UNREGISTER);
			}
			
			//generate token
			String token = TokenUtils.createToken(username);
			//check token exists
			TabToken tabtoken = tabTokenDao.findByUsername(username);
			if(tabtoken != null){
				tabtoken.setToken(token);
				tabTokenDao.save(tabtoken);
			}
			else{
				tabtoken = new TabToken();
				tabtoken.setUsername(username);
				tabtoken.setToken(token);
				tabTokenDao.save(tabtoken);
			}
			
			//add login log
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest(); 
	    	String clientIp = request.getRemoteAddr();
	    	log.debug("====clientIp==="+clientIp);
	    	
	    	TabLog tabLog = new TabLog();
	    	tabLog.setLoginip(clientIp);
	    	tabLog.setLogintime(getCurrentTimeStamp());
	    	tabLog.setToken(token);
	    	tabLog.setUsername(username); 
	    	tabLogDao.save(tabLog);
	    	
			Token tokenEn = new Token();
			tokenEn.setToken(token);
			tokenEn.setExpiresIn(ConstantsData.TOKEN_EXPIRES);
			
			return RespDTO.success("操作成功", gson.toJson(tokenEn));
		}
		catch(Exception e){
			log.error(e.getMessage());
			return RespDTO.fail("操作失败",ConstantsData.RTN_SERVER_INTERNAL_ERROR);
		}
	}
	
	public RespDTO<String> logout(String body){
		try{
			JsonObject obj = gson.fromJson(body, JsonObject.class);
			String username = obj.get("username").getAsString();
			String token = obj.get("token").getAsString();
			
			//compare token with db
			TabToken tabToken = tabTokenDao.findByUsername(username);
			if(tabToken != null){
				if(!token.equals(tabToken.getToken())){
					return RespDTO.fail("操作失败",ConstantsData.RTN_UNLOGIN);
				}
			}
			else{
				return RespDTO.fail("操作失败",ConstantsData.RTN_UNLOGIN);
			}
			
			//verify token
			//FIXME add other verify ???
			JwtToken jwt = TokenUtils.parseToken(token);
			if(!username.equals(jwt.getId())){
				return RespDTO.fail("操作失败",ConstantsData.RTN_UNLOGIN);
			}
			
			//delete token
			tabTokenDao.delete(tabToken.getId());
			
			//update logouttime
			TabLog tabLog = tabLogDao.findByToken(token);
			tabLog.setLogouttime(getCurrentTimeStamp());
			tabLogDao.save(tabLog);
			
		}catch(JsonSyntaxException |NullPointerException e){
			log.error(e.getMessage());
			return RespDTO.fail("操作失败",ConstantsData.RTN_INVALID_ARGS);
		}
		catch(Exception e){
			log.error(e.getMessage());
			return RespDTO.fail("操作失败",ConstantsData.RTN_SERVER_INTERNAL_ERROR);
		}
		return RespDTO.success("操作成功", null);
	}
	
	public RespDTO<String> allow(String body){
		try{
			JsonObject obj = gson.fromJson(body, JsonObject.class);
			String username = obj.get("username").getAsString();
			String token = obj.get("token").getAsString();
			JwtToken jwt = TokenUtils.parseToken(token);
			Date expire = jwt.getExpiratioin();
			String authUser = jwt.getId();
			Date now = new Date();
			if(expire.before(now)){//expired
				return RespDTO.fail("操作失败",ConstantsData.RTN_LOGIN_EXPIRED);
			}
			//unlogin
			TabToken tabToken = tabTokenDao.findByUsername(authUser);
			if(tabToken != null){
				if(!token.equals(tabToken.getToken())){
					return RespDTO.fail("操作失败",ConstantsData.RTN_UNLOGIN);
				}
			}
			else{
				return RespDTO.fail("操作失败",ConstantsData.RTN_UNLOGIN);
			}
			String txId = chaincodeService.invoke("auth", new String[]{authUser,username});
			log.debug("===txID==="+txId);
			if(txId == null){
				return RespDTO.fail("操作失败",ConstantsData.RTN_SERVER_INTERNAL_ERROR);
			}
		}
		catch(JsonSyntaxException |NullPointerException e){
			log.error(e.getMessage());
			return RespDTO.fail("操作失败",ConstantsData.RTN_INVALID_ARGS);
		}
		catch(Exception e){
			log.error(e.getMessage());
			return RespDTO.fail("操作失败",ConstantsData.RTN_SERVER_INTERNAL_ERROR);
		}
		return RespDTO.success("操作成功");
	}
	
	public RespDTO<String> revoke(String body){
		try{
			JsonObject obj = gson.fromJson(body, JsonObject.class);
			String username = obj.get("username").getAsString();
			String token = obj.get("token").getAsString();
			JwtToken jwt = TokenUtils.parseToken(token);
			Date expire = jwt.getExpiratioin();
			String authUser = jwt.getId();
			Date now = new Date();
			if(expire.before(now)){//expired
				return RespDTO.fail("操作失败",ConstantsData.RTN_LOGIN_EXPIRED);
			}
			//unlogin
			TabToken tabToken = tabTokenDao.findByUsername(authUser);
			if(tabToken != null){
				if(!token.equals(tabToken.getToken())){
					return RespDTO.fail("操作失败",ConstantsData.RTN_UNLOGIN);
				}
			}
			else{
				return RespDTO.fail("操作失败",ConstantsData.RTN_UNLOGIN);
			}
			
			String txId = chaincodeService.invoke("revoke", new String[]{authUser,username});
			log.debug("===txID==="+txId);
			if(txId == null){
				return RespDTO.fail("操作失败",ConstantsData.RTN_SERVER_INTERNAL_ERROR);
			}
		}
		catch(JsonSyntaxException | NullPointerException e){
			log.error(e.getMessage());
			return RespDTO.fail("操作失败",ConstantsData.RTN_INVALID_ARGS);
		}
		catch(Exception e){
			log.error(e.getMessage());
			return RespDTO.fail("操作失败",ConstantsData.RTN_SERVER_INTERNAL_ERROR);
		}
		return RespDTO.success("操作成功");
	}
	
	private static java.sql.Timestamp getCurrentTimeStamp() {  
		   
	    java.util.Date today = new java.util.Date();  
	    System.out.println(""+today.getTime());
	    return new java.sql.Timestamp(today.getTime());  
	   
	} 
}
