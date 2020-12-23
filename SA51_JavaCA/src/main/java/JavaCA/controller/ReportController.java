package JavaCA.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import JavaCA.model.Product;
import JavaCA.model.TransactionDetail;
import JavaCA.service.ProductService;
import JavaCA.service.ProductServiceImpl;
import JavaCA.service.ReportService;
import JavaCA.service.ReportServiceImpl;
import JavaCA.service.SupplierService;
import JavaCA.service.SupplierServiceImpl;
import JavaCA.service.TransactionDetailsService;
import JavaCA.service.UserServiceImpl;
import JavaCA.service.UserService;

@Controller
@RequestMapping("/report")
public class ReportController 
{
	private ProductServiceImpl pservice;
	private TransactionDetailsService tdservice;
	private SupplierService sservice;
	private UserService uservice;
	private ReportService rservice;
	private HttpSession session;
		
	@Autowired
	public void setServices(ProductServiceImpl pservice, TransactionDetailsService tdservice, 
			SupplierServiceImpl sservice, UserServiceImpl uservice, ReportServiceImpl rservice, HttpSession session) 
	{
		this.pservice = pservice;
		this.tdservice = tdservice;
		this.sservice = sservice;
		this.uservice = uservice;
		this.rservice = rservice;
		this.session = session;
	}
	
	@RequestMapping(value={"/usage"}, method=RequestMethod.GET)
	public String usageReportForProduct(Model model)
	{
		//check if user is admin, otherwise redirect
		if(!uservice.verifyAdmin(session)) {
			return "redirect:/";
		}
		
		return "report/usage";
	}
	
	@RequestMapping(value={"/search"}, method=RequestMethod.POST)
	public String searchUsageReportForProduct(RedirectAttributes model, @RequestParam String id, @RequestParam String fromDate, 
						   @RequestParam String toDate)
	{
		if (!ProductService.isProductIdNumeric(id) || pservice.findProduct(Integer.parseInt(id)) == null)
			model.addFlashAttribute("errorMsgId", "This is not a valid product id");
		else
			model.addFlashAttribute("id", Integer.parseInt(id));
		if (!TransactionDetailsService.isValidDateFormat(fromDate))
			model.addFlashAttribute("errorMsgFromDate", "Input must be in the format of yyyy-MM-dd");
		else
		{
			if (!fromDate.isBlank())
				model.addFlashAttribute("fromDate", Date.valueOf(fromDate));
		}
		if (!TransactionDetailsService.isValidDateFormat(toDate))
			model.addFlashAttribute("errorMsgToDate", "Input must be in the format of yyyy-MM-dd");
		else
		{
			if (!toDate.isBlank())
				model.addFlashAttribute("toDate", Date.valueOf(toDate));
		}
		if (!ProductService.isProductIdNumeric(id) || pservice.findProduct(Integer.parseInt(id)) == null 
			|| !TransactionDetailsService.isValidDateFormat(fromDate) || !TransactionDetailsService.isValidDateFormat(toDate))
			return "redirect:/report/usage/";
		model.addFlashAttribute("search", true);
		return "redirect:/report/usage/" + id;
	}
	
	@RequestMapping(value={"/usage/{id}"}, method=RequestMethod.GET)
	public String usageReportForProductId(Model model, @PathVariable long id)
	{
		//check if user is admin, otherwise redirect
		if(!uservice.verifyAdmin(session)) {
			return "redirect:/";
		}
		
		String output = "report/usage";
		Date fromDate = (Date) model.getAttribute("fromDate");
		Date toDate = (Date) model.getAttribute("toDate");
		List<TransactionDetail> transactionDetailsForThisProduct = tdservice.findTransactionDetailsByProductId(id);
		if (fromDate == null && toDate == null)
		{
			model.addAttribute("transactiondetails", transactionDetailsForThisProduct);
		}
		else
		{
			if(fromDate != null && toDate != null)
			{
				transactionDetailsForThisProduct = tdservice.findAllTransactionDetailsForProductBetweenDateRange(id, fromDate, toDate);
				model.addAttribute("transactiondetails", transactionDetailsForThisProduct);
			}
			else if (model.getAttribute("fromDate") != null)
			{
				transactionDetailsForThisProduct = tdservice.findAllTransactionDetailsForProductFromDate(id, fromDate);
				model.addAttribute("transactiondetails", transactionDetailsForThisProduct);
			}
			else
			{
				transactionDetailsForThisProduct = tdservice.findAllTransactionDetailsForProductUpToDate(id, toDate);
				model.addAttribute("transactiondetails", transactionDetailsForThisProduct);
			}
		}
		model.addAttribute("product", pservice.findProduct(id));
		if (model.containsAttribute("print"))
			output = "report/usagereportprint";
			model.addAttribute("timeOfReport", Date.valueOf(LocalDate.now()));
		return output;
	}
	
	@RequestMapping(value={"/usage/{id}/print"}, method=RequestMethod.GET)
	public String printUsageReportForProductId(@PathVariable long id, RedirectAttributes model)
	{
		//check if user is admin, otherwise redirect
		if(!uservice.verifyAdmin(session)) {
			return "redirect:/";
		}
		
		model.addFlashAttribute("print", true);
		return "redirect:/report/usage/" + id;
	}
	
	@RequestMapping(value={"/reorder"}, method=RequestMethod.GET)
	public String reorderReport(Model model)
	{
		//check if user is admin, otherwise redirect
		if(!uservice.verifyAdmin(session)) {
			return "redirect:/";
		}
		
		String output = "report/reorder";
		Map<Double, List<List<Product>>> reorderReport = rservice.reorderReportData();
		model.addAttribute("productsThatRequireReorder", reorderReport.get(reorderReport.keySet().toArray()[0]));
		model.addAttribute("grandTotal", reorderReport.keySet().toArray()[0]);
		if (model.containsAttribute("print"))
			output = "report/reorderreportprint";
			model.addAttribute("timeOfReport", Date.valueOf(LocalDate.now()));
		return output;
	}
	
	@RequestMapping(value={"/reorder/print"}, method=RequestMethod.GET)
	public String printReorderReport(RedirectAttributes model)
	{
		//check if user is admin, otherwise redirect
		if(!uservice.verifyAdmin(session)) {
			return "redirect:/";
		}
		
		model.addFlashAttribute("print", true);
		return "redirect:/report/reorder/";
	}
}
