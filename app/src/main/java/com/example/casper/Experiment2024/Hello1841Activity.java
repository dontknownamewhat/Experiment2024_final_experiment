package com.example.casper.Experiment2024;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.Objects;


public class Hello1841Activity extends AppCompatActivity {
    private ArrayList<Item> items;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private RecyclerView mainRecyclerView;
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = item.getOrder(); // 获取选中的位置

        switch (item.getItemId()) {
            case 0: // 添加
                // 启动添加Activity
                Intent addIntent = new Intent(this, BookDetailsActivity.class);
                activityResultLauncher.launch(addIntent);
                break;
            case 1: // 删除
                new AlertDialog.Builder(this)
                        .setTitle("确认删除")
                        .setMessage("您确定要删除这本书吗？")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            items.remove(position);
                            Objects.requireNonNull(mainRecyclerView.getAdapter()).notifyItemRemoved(position);
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                break;
            case 2: // 修改
                // 启动修改Activity并传递当前书籍信息
                Item currentItem = items.get(position);
                Intent editIntent = new Intent(this, BookDetailsActivity.class);
                editIntent.putExtra("item", (CharSequence) currentItem); // 传递当前书籍对象
                activityResultLauncher.launch(editIntent);
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // 使用布局文件创建控件
        setContentView(R.layout.activity_hello1841);

        RecyclerView mainRecyclerView = findViewById(R.id.recyclerview_items);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        items = new ArrayList<>();
        items.add(new Item("book1", 1.5, R.drawable.book_1));
        items.add(new Item("book2", 2.5, R.drawable.book_2));
        items.add(new Item("book_no_name", 3.5, R.drawable.book_no_name));

        ShopItemAdapter shopItemAdapter = new ShopItemAdapter(items);
        mainRecyclerView.setAdapter(shopItemAdapter);

        registerForContextMenu(mainRecyclerView);

        // 初始化 ActivityResultLauncher
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        Item item = (Item) data.getSerializableExtra("item");
                        int position = data.getIntExtra("position", -1);

                        if (position == -1) {
                            // 添加新项
                            items.add(item);
                            shopItemAdapter.notifyItemInserted(items.size() - 1);
                        } else {
                            // 修改现有项
                            items.set(position, item);
                            shopItemAdapter.notifyItemChanged(position);
                        }
                    }
                }
        );

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private class ShopItemAdapter extends RecyclerView.Adapter {
        private final List<Item> items;

        public ShopItemAdapter(List<Item> items) {
            this.items=items;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_book, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MyViewHolder myViewHolder= (MyViewHolder) holder;
            myViewHolder.getTextViewName().setText(items.get(position).getTitle());
            myViewHolder.getTextViewPrice().setText("" + items.get(position).getPrice());
            myViewHolder.getImageViewPicture().setImageResource(items.get(position).getResourceId());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
            public ImageView getImageViewPicture() {
                return imageViewPicture;
            }

            public TextView getTextViewName() {
                return textViewName;
            }

            public TextView getTextViewPrice() {
                return textViewPrice;
            }

            private final ImageView imageViewPicture;
            private final TextView textViewName;
            private final TextView textViewPrice;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.imageViewPicture= itemView.findViewById(R.id.imageview_item);
                this.textViewName= itemView.findViewById(R.id.textview_item_name);
                this.textViewPrice=itemView.findViewById(R.id.textview_item_price);

                itemView.setOnCreateContextMenuListener(this);
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.setHeaderTitle("具体操作");

                menu.add(0, 0, this.getAdapterPosition(), "添加" + this.getAdapterPosition());
                menu.add(0, 1, this.getAdapterPosition(), "删除" + this.getAdapterPosition());
                menu.add(0, 2, this.getAdapterPosition(), "修改" + this.getAdapterPosition());
            }
        }
    }
}