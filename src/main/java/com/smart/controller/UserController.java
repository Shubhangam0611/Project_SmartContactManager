package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entites.Contact;
import com.smart.entites.User;
import com.smart.helper.Message;

import net.bytebuddy.asm.Advice.This;


@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	//methodod for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal)
	{
		String userName = principal.getName();
		System.out.println("USERNAME" + userName);
		
		//get the user using username()email
		
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER"+user);
		model.addAttribute("user",user);
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{	
		model.addAttribute("title","User DashBoard");
		return "normal/user_dashboard";
	}
	
	//createing open add fprm handler
	
	@RequestMapping("/add-contact")
	public String openAddContactForm(Model model) 
	{
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		
		return "normal/add_contact_form";
		
	}
	
	//proccesing add contact form
	
	@RequestMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			                     @RequestParam("profileImage") MultipartFile file ,
			                     Principal principal,HttpSession session)
	{
		try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		//proccesing and uploading file (image)
		
		if (file.isEmpty()) {
			System.out.println("File is empty");
			contact.setImage("contact.png");
		}
		else {
			   contact.setImage(file.getOriginalFilename());
			   
			File savefile =  new ClassPathResource("static/img").getFile();
			
		Path path =	Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
		
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		}
		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		System.out.println("DATA"+contact);
		
		System.out.println("Added to data base");
		
		//success message
		session.setAttribute("message",new Message("Your contact is added !! Add newone..", "success"));
		
		}catch (Exception e) {
			System.out.println("ERROR"+e.getMessage());
			e.printStackTrace();
			//error message
			session.setAttribute("message",new Message("Something went wrong!! Try again..", "danger"));
		}
		
		return "normal/add_contact_form";
	}
	
       //show contact handler
	//pegination code
	//per page=5[n]
	//current page =0[page]
	
	@RequestMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m, Principal principal ) {
		
		m.addAttribute("title","View Contacts");
		//for sending contact list
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		//current page
		//contact per page =5
		PageRequest pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findContactsByuser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
	    m.addAttribute("currentPage",page);
	    m.addAttribute("totalPages",contacts.getTotalPages());
	    
		return "normal/show_contacts";
		
	}
	//showing  specific contact detail
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal)
	{
		System.out.println("CID "+cId);
		
		Optional<Contact> contactoptional = this.contactRepository.findById(cId);
		Contact contact = contactoptional.get();
		
		//securitybreker start
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId())
		{ 
			model.addAttribute("contact",contact);
			model.addAttribute("title", contact.getName());
		
		}
		return "normal/contact_detail";
	}
	//delete contact handler
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session,Principal principal)
	{
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		User user = this.userRepository.getUserByUserName(principal.getName());	
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		System.out.println("DELETED");
		
		session.setAttribute("message",new Message("Contact deleted succefully...","success"));
		
		return "redirect:/user/show_contacts/0";
	}
	
	//update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m) 
	{
	    m.addAttribute("title","Update Contact");
	    Contact contact =  this.contactRepository.findById(cid).get();
	    m.addAttribute("contact",contact);
	    
		return"normal/update_form";	
	}
	//update contact handler
	
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,
			                    @RequestParam("profileImage") MultipartFile file,
			                    Model m,HttpSession session,
			                    Principal principal)
	{
		try { 
			//image
			if(!file.isEmpty())
			{
				//file work
				//rewrite
				
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated...", "success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		System.out.println("Contact Name "+ contact.getName());
		System.out.println("Contact ID "+ contact.getcId());
		return"/user/"+contact.getcId()+"/contact" ;	
	}
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title","Your Profile");
	   return "normal/profile";	
	}
	
}
