package com.app.tasteit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText etSearch;
    Button btnSearch;
    LinearLayout categoriesRow;
    RecyclerView rvRecipes;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    private final String[] categories = {
            "Pastas", "Carnes", "Veggie", "Postres", "Sopas",
            "Arroces", "Ensaladas", "Pescados & Mariscos", "Tapas & Snacks", "Sin TACC"
    };

    private final Object[][] recipesData = {
            {"Spaghetti Bolognesa","Pastas","Clásica pasta italiana con salsa de carne y tomate.",
                    "https://images.unsplash.com/photo-1601924638867-3ec62c1aa5b1","30 min"},
            {"Fettuccine Alfredo","Pastas","Crema, manteca y parmesano para una salsa sedosa.",
                    "https://images.unsplash.com/photo-1589302168068-964664d93dc0","25 min"},
            {"Lasagna de Verduras","Pastas","Capas de vegetales asados y bechamel.",
                    "https://images.unsplash.com/photo-1603133872878-684f589c24f0","40 min"},
            {"Pollo al horno","Carnes","Jugoso pollo al horno con especias.",
                    "https://images.unsplash.com/photo-1604908816370-9c2b6f3f2d2f","50 min"},
            {"Asado clásico","Carnes","Costillar con chimichurri y fuego lento.",
                    "https://images.unsplash.com/photo-1613145997970-db84a7975fbb","2 hs"},
            {"Ensalada César","Ensaladas","Lechuga, pollo, crutones y aderezo César.",
                    "https://images.unsplash.com/photo-1555939594-58d7cb561ad1","15 min"},
            {"Tarta de Manzana","Postres","Masa hojaldrada y relleno de manzana.",
                    "https://images.unsplash.com/photo-1601004890684-d8cbf643f5f2","1h"},
            {"Paella Valenciana","Arroces","Arroz con mariscos, pollo y vegetales.",
                    "https://images.unsplash.com/photo-1604908177225-1e29cf8c2c4c","1h 15min"}
    };

    private RecipeAdapter adapter;
    private String activeCategory = null;
    private SharedPreferences sharedPrefs;
    private boolean showingFavorites = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        sharedPrefs = getSharedPreferences("FavoritesPrefs", Context.MODE_PRIVATE);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_recetas) {
                showingFavorites = false;
                adapter.setRecipes(getAllRecipes());
                createCategoryButtons();
            } else if (id == R.id.nav_comunidad) {
                Toast.makeText(this, getString(R.string.section_community), Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_favoritos) {
                showingFavorites = true;
                loadFavorites();
            } else if (id == R.id.nav_logout) {
                LoginActivity.currentUser = null;
                Toast.makeText(this, getString(R.string.session_closed), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        categoriesRow = findViewById(R.id.categoriesLayout);
        rvRecipes = findViewById(R.id.rvRecipes);

        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, getAllRecipes(), showingFavorites);
        rvRecipes.setAdapter(adapter);

        createCategoryButtons();

        btnSearch.setOnClickListener(v -> {
            String q = etSearch.getText().toString().trim().toLowerCase();
            if (q.isEmpty()) adapter.setRecipes(showingFavorites ? getFavoriteRecipes() : getAllRecipes());
            else adapter.setRecipes(searchRecipes(q));
        });

        ImageView ivAccount = findViewById(R.id.ivAccount);
        ivAccount.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, ivAccount);
            if(LoginActivity.currentUser == null) menu.getMenu().add("Login");
            else menu.getMenu().add("Logout");

            menu.setOnMenuItemClickListener(item -> {
                if(item.getTitle().equals("Login")) startActivity(new Intent(this, LoginActivity.class));
                else {
                    Toast.makeText(this, getString(R.string.session_closed), Toast.LENGTH_SHORT).show();
                    LoginActivity.currentUser = null;
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
                return true;
            });
            menu.show();
        });
    }

    private List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        for (Object[] data : recipesData) {
            list.add(new Recipe(
                    (String) data[0],
                    (String) data[2],
                    (String) data[3],
                    (String) data[4]
            ));
        }
        return list;
    }

    private List<Recipe> searchRecipes(String q) {
        List<Recipe> list = new ArrayList<>();
        for (Object[] data : recipesData) {
            if(((String)data[0]).toLowerCase().contains(q)) {
                list.add(new Recipe(
                        (String)data[0],
                        (String)data[2],
                        (String)data[3],
                        (String)data[4]
                ));
            }
        }
        return list;
    }

    private List<Recipe> filterByCategory(String cat) {
        List<Recipe> list = new ArrayList<>();
        for (Object[] data : recipesData) {
            if(((String)data[1]).equals(cat)) {
                list.add(new Recipe(
                        (String)data[0],
                        (String)data[2],
                        (String)data[3],
                        (String)data[4]
                ));
            }
        }
        return list;
    }

    private List<Recipe> getFavoriteRecipes() {
        String currentUser = LoginActivity.currentUser;
        if(currentUser == null) return new ArrayList<>();
        String key = "favorites_" + currentUser;
        String json = sharedPrefs.getString(key, null);
        Type type = new TypeToken<List<Recipe>>(){}.getType();
        return json == null ? new ArrayList<>() : new Gson().fromJson(json, type);
    }

    private void createCategoryButtons() {
        categoriesRow.removeAllViews();
        if(showingFavorites) return;

        for(String cat : categories) {
            Button b = new Button(this);
            b.setText(cat);
            b.setAllCaps(false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(12,6,12,6);
            b.setLayoutParams(lp);

            b.setOnClickListener(v -> {
                if(cat.equals(activeCategory)) {
                    activeCategory = null;
                    adapter.setRecipes(getAllRecipes());
                } else {
                    activeCategory = cat;
                    adapter.setRecipes(filterByCategory(cat));
                }
            });
            categoriesRow.addView(b);
        }
    }

    private void loadFavorites() {
        List<Recipe> favorites = getFavoriteRecipes();
        if(favorites.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_favorites), Toast.LENGTH_SHORT).show();
        }
        adapter.setRecipes(favorites);
        createCategoryButtons();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle != null && toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }
}