package com.campushelp.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campushelp.user.dto.AddressRequest;
import com.campushelp.user.entity.ChAddress;
import com.campushelp.user.exception.BadRequestException;
import com.campushelp.user.mapper.ChAddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AddressService {

    private final ChAddressMapper addressMapper;

    public AddressService(ChAddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public List<ChAddress> listMine(long userId) {
        return addressMapper.selectList(
                new QueryWrapper<ChAddress>().eq("user_id", userId).orderByDesc("is_default").orderByDesc("updated_at"));
    }

    @Transactional(rollbackFor = Exception.class)
    public ChAddress create(long userId, AddressRequest req) {
        LocalDateTime now = LocalDateTime.now();
        boolean asDefault = Boolean.TRUE.equals(req.getDefaultAddress());
        if (asDefault) {
            clearDefault(userId);
        }
        ChAddress a = new ChAddress();
        a.setUserId(userId);
        a.setCampusId(req.getCampusId());
        a.setBuildingId(req.getBuildingId());
        a.setContactName(req.getContactName().trim());
        a.setContactPhone(req.getContactPhone().trim());
        a.setDetail(req.getDetail().trim());
        a.setLabel(req.getLabel() == null ? null : req.getLabel().trim());
        a.setIsDefault(asDefault ? 1 : 0);
        a.setCreatedAt(now);
        a.setUpdatedAt(now);
        addressMapper.insert(a);
        return a;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChAddress update(long userId, long addressId, AddressRequest req) {
        ChAddress existing = getOwnedOrThrow(userId, addressId);
        boolean asDefault = Boolean.TRUE.equals(req.getDefaultAddress());
        if (asDefault) {
            clearDefault(userId);
        }
        existing.setCampusId(req.getCampusId());
        existing.setBuildingId(req.getBuildingId());
        existing.setContactName(req.getContactName().trim());
        existing.setContactPhone(req.getContactPhone().trim());
        existing.setDetail(req.getDetail().trim());
        existing.setLabel(req.getLabel() == null ? null : req.getLabel().trim());
        existing.setIsDefault(asDefault ? 1 : 0);
        existing.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(existing);
        return existing;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(long userId, long addressId) {
        ChAddress existing = getOwnedOrThrow(userId, addressId);
        addressMapper.deleteById(existing.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public ChAddress setDefault(long userId, long addressId) {
        ChAddress existing = getOwnedOrThrow(userId, addressId);
        clearDefault(userId);
        existing.setIsDefault(1);
        existing.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(existing);
        return existing;
    }

    private ChAddress getOwnedOrThrow(long userId, long addressId) {
        ChAddress a = addressMapper.selectById(addressId);
        if (a == null || !a.getUserId().equals(userId)) {
            throw new BadRequestException("地址不存在");
        }
        return a;
    }

    private void clearDefault(long userId) {
        ChAddress u = new ChAddress();
        u.setIsDefault(0);
        u.setUpdatedAt(LocalDateTime.now());
        addressMapper.update(u, new UpdateWrapper<ChAddress>().eq("user_id", userId).eq("is_default", 1));
    }
}
