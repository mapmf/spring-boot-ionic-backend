package com.marcos.cursomc.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcos.cursomc.domain.Cliente;
import com.marcos.cursomc.domain.ItemPedido;
import com.marcos.cursomc.domain.PagamentoComBoleto;
import com.marcos.cursomc.domain.Pedido;
import com.marcos.cursomc.domain.enums.EstadoPagamento;
import com.marcos.cursomc.repositories.ItemPedidoRepository;
import com.marcos.cursomc.repositories.PagamentoRepository;
import com.marcos.cursomc.repositories.PedidoRepository;
import com.marcos.cursomc.security.UserSS;
import com.marcos.cursomc.services.exceptions.AuthorizationException;
import com.marcos.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {

	@Autowired
	private PedidoRepository repo;

	@Autowired
	private BoletoService boletoService;
	
	@Autowired
	private PagamentoRepository pagamentoRepository;
	
	@Autowired
	private ProdutoService produtoService;
	
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	
	@Autowired
	private ClienteService clienteService;

	@Autowired
	private EmailService emailService;
	
	public Pedido find(Integer id) {

		return repo.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException
						("Pedido com id " + id + " não foi encontrado"
								+ ", Tipo: " + Pedido.class.getName()));
	}

	@Transactional
	public Pedido insert(Pedido obj) {

		obj.setId(null);
		obj.setInstante(new Date());
		
		obj.setCliente(clienteService.find(obj.getCliente().getId()));
		
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		
		if(obj.getPagamento() instanceof PagamentoComBoleto) {
			
			PagamentoComBoleto pagto = (PagamentoComBoleto)obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, obj.getInstante());
		}
		
		obj = repo.save(obj);
		
		pagamentoRepository.save(obj.getPagamento()); 
		
		for (ItemPedido itemPedido: obj.getItens()) {
			
			itemPedido.setProduto(produtoService.find(itemPedido.getProduto().getId()));
			itemPedido.setDesconto(0.0);
			itemPedido.setPreco(itemPedido.getProduto().getPreco());
			
			itemPedido.setPedido(obj);
		}
		
		itemPedidoRepository.saveAll(obj.getItens());
		
		//emailService.sendOrderConfirmationEmail(obj);
		emailService.sendOrderConfirmationHtmlEmail(obj);
		
		return obj;
	}
	
	public Page<Pedido> findPageByCliente(int page, int size, String orderBy, String direction){
		
		UserSS user = UserService.authenticated();
		
		if(user == null) {
			throw new AuthorizationException("Acesso negado");
		}

		Cliente cliente = clienteService.find(user.getId());
		
		PageRequest pageRequest = PageRequest.of(page, size, Direction.valueOf(direction), orderBy);
		
		return repo.findByCliente(cliente, pageRequest);
	}

}
