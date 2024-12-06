package com.example.casper.Experiment2024;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookListFragment extends Fragment {
    private ArrayList<Item> items;
    private RecyclerView mainRecyclerView;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 使用布局文件创建控件
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);

        mainRecyclerView = view.findViewById(R.id.recyclerview_items);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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

        return view;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = item.getOrder(); // 获取选中的位置

        switch (item.getItemId()) {
            case 0: // 添加
                // 启动添加Activity
                Intent addIntent = new Intent(getContext(), BookDetailsActivity.class);
                activityResultLauncher.launch(addIntent);
                break;
            case 1: // 删除
                new AlertDialog.Builder(getContext())
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
                Intent editIntent = new Intent(getContext(), BookDetailsActivity.class);
                editIntent.putExtra("item", (CharSequence) currentItem); // 传递当前书籍对象
                activityResultLauncher.launch(editIntent);
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    private class ShopItemAdapter extends RecyclerView.Adapter<ShopItemAdapter.MyViewHolder> {
        private final List<Item> items;

        public ShopItemAdapter(List<Item> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_book, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.getTextViewName().setText(items.get(position).getTitle());
            holder.getTextViewPrice().setText("" + items.get(position).getPrice());
            holder.getImageViewPicture().setImageResource(items.get(position).getResourceId());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
            private final ImageView imageViewPicture;
            private final TextView textViewName;
            private final TextView textViewPrice;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.imageViewPicture = itemView.findViewById(R.id.imageview_item);
                this.textViewName = itemView.findViewById(R.id.textview_item_name);
                this.textViewPrice = itemView.findViewById(R.id.textview_item_price);

                itemView.setOnCreateContextMenuListener(this);
            }

            public ImageView getImageViewPicture() {
                return imageViewPicture;
            }

            public TextView getTextViewName() {
                return textViewName;
            }

            public TextView getTextViewPrice() {
                return textViewPrice;
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MenuInflater inflater = requireActivity().getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                menu.setHeaderTitle("具体操作");

                menu.add(0, 0, this.getAdapterPosition(), "添加" + this.getAdapterPosition());
                menu.add(0, 1, this.getAdapterPosition(), "删除" + this.getAdapterPosition());
                menu.add(0, 2, this.getAdapterPosition(), "修改" + this.getAdapterPosition());
            }
        }
    }
}