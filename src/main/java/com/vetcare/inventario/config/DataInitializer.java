package com.vetcare.inventario.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vetcare.inventario.entity.Categoria;
import com.vetcare.inventario.entity.Proveedor;
import com.vetcare.inventario.entity.Rol;
import com.vetcare.inventario.entity.Usuario;
import com.vetcare.inventario.repository.CategoriaRepository;
import com.vetcare.inventario.repository.ProveedorRepository;
import com.vetcare.inventario.repository.RolRepository;
import com.vetcare.inventario.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner cargarDatosIniciales(RolRepository roles,
                                           UsuarioRepository usuarios,
                                           CategoriaRepository categorias,
                                           ProveedorRepository proveedores,
                                           PasswordEncoder codificador) {
        return args -> {
            if (roles.count() == 0) {
                List.of("ADMINISTRADOR", "ENCARGADO", "VENTAS", "GERENTE")
                        .forEach(nombre -> roles.save(rol(nombre)));
            }
            if (usuarios.count() == 0) {
                usuarios.save(usuario(roles, codificador, "admin", "Administrador VetCare",
                        "Admin123*", "ADMINISTRADOR"));
                usuarios.save(usuario(roles, codificador, "encargado", "Encargado de Inventario",
                        "Encargado123*", "ENCARGADO"));
                usuarios.save(usuario(roles, codificador, "ventas", "Personal de Ventas",
                        "Ventas123*", "VENTAS"));
                usuarios.save(usuario(roles, codificador, "gerente", "Gerente VetCare",
                        "Gerente123*", "GERENTE"));
            }
            if (categorias.count() == 0) {
                categorias.saveAll(List.of(
                        categoria("Alimentos", "Alimento para mascotas"),
                        categoria("Medicamentos", "Medicamentos veterinarios"),
                        categoria("Antiparasitarios", "Control de pulgas, garrapatas y parásitos"),
                        categoria("Vitaminas", "Vitaminas y suplementos"),
                        categoria("Higiene", "Artículos de higiene y cuidado"),
                        categoria("Accesorios", "Collares, correas y otros accesorios")));
            }
            if (proveedores.count() == 0) {
                proveedores.saveAll(List.of(
                        proveedor("Distribuidora Pet Andina S.A.C.", "20111111111",
                                "014000001", "ventas@petandina.example"),
                        proveedor("Laboratorios Vet Sur S.A.", "20222222222",
                                "014000002", "pedidos@vetsur.example"),
                        proveedor("Importaciones Mascota Feliz E.I.R.L.", "20333333333",
                                "014000003", "contacto@mascotafeliz.example")));
            }
        };
    }

    private Rol rol(String nombre) {
        Rol r = new Rol();
        r.setNombre(nombre);
        return r;
    }

    private Usuario usuario(RolRepository roles, PasswordEncoder codificador, String username,
                            String nombreCompleto, String clave, String nombreRol) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombreCompleto(nombreCompleto);
        u.setPasswordHash(codificador.encode(clave));
        u.setRol(roles.findByNombre(nombreRol).orElseThrow());
        return u;
    }

    private Categoria categoria(String nombre, String descripcion) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setDescripcion(descripcion);
        return c;
    }

    private Proveedor proveedor(String razonSocial, String ruc, String telefono, String correo) {
        Proveedor p = new Proveedor();
        p.setRazonSocial(razonSocial);
        p.setRuc(ruc);
        p.setTelefono(telefono);
        p.setCorreo(correo);
        return p;
    }
}
